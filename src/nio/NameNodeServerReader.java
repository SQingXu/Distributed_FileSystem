package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import directory.DFile;
import directory.DirectoryController;
import directory.NameDirFileObject;
import main.CommandParsingHelper;
import niocmd.NIOCommand;
import niocmd.NIOCommandFactory;
import niocmd.NIOCommandType;
import niocmd.SendFileObject;

class DFileReceive{
	public DFile file;
	public int left_to_receive;
	public boolean first_to_receive;
	public DFileReceive(DFile file, int t) {
		this.file =file;
		this.left_to_receive = t;
		this.first_to_receive = true;
	}
}

public class NameNodeServerReader implements Runnable{
	public BlockingQueue<ByteBufferWSource> buffer_queue;
	public ServerWriter writer;
	public ConcurrentHashMap<SocketChannel, AcumulateBuffer> channelAcumBuffers;
	public Map<UUID, DFileReceive> pending_files;
	public NameNodeServer server;
	public NameNodeServerReader(int queue_size, ServerWriter writer, NameNodeServer server) {
		buffer_queue = new ArrayBlockingQueue<>(queue_size);
		this.writer = writer;
		this.server = server;
		channelAcumBuffers = new ConcurrentHashMap<>();
		pending_files = new HashMap<>();
		
	}
	
	public void addBufferWSource(ByteBufferWSource buffer_source) throws InterruptedException{
		if(!channelAcumBuffers.containsKey(buffer_source.channel)) {
			channelAcumBuffers.put(buffer_source.channel, new AcumulateBuffer());
		}
		buffer_queue.put(buffer_source);
		return;
	}
	
	public void channelClosed(SocketChannel channel) {
		channelAcumBuffers.remove(channel);
	}

	@Override
	public void run() {
		while(true) {
			if(!buffer_queue.isEmpty()) {
				try {
					ByteBufferWSource buffer_source = buffer_queue.take();
					SocketChannel channel = buffer_source.channel;
					ByteBuffer buffer = buffer_source.buffer;
					AcumulateBuffer ab = channelAcumBuffers.get(channel);
					while(buffer.remaining() > 0) {
						if(ab.acumulate(buffer)) {
							processCmd(ab.gatheredBuffer, channel);
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		
	}
	
	private void processCmd(ByteBuffer buffer, SocketChannel channel) {
		String command_str = new String(buffer.array(),buffer.position(), buffer.remaining());
		//System.out.println("Read string is: " + command_str);
		NIOCommand cmd;
		try {
			cmd = (NIOCommand)NIOSerializer.FromString(command_str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		
		NIOCommand feedback = new NIOCommand(NIOCommandType.RESULT_FEED, new String[1]);
		if(cmd.type.equals(NIOCommandType.DOWNLOAD_FILE_NAME)) {
			boolean result = downloadCmdProcess(cmd, feedback);
			if(!result) {
				writer.writeToChannel(feedback, channel);
			}
			return;
		}else if(cmd.type.equals(NIOCommandType.UPLOAD_FILE_NAME)) {
			boolean result = uploadCmdProcess(cmd,channel, feedback);
			if(!result) {
				writer.writeToChannel(feedback, channel);
			}
			return;
		}else if(cmd.type.equals(NIOCommandType.SEND_FILE_FEED)) {
			if(Boolean.parseBoolean(cmd.args[0])) {
				//DirectoryController.instance.createFile(fname, path, file_addrs);
			}
		}else if(cmd.type.equals(NIOCommandType.RECEIVE_DATA_FEED)) {
			boolean result = receiveFeedCmdProcess(cmd, feedback, channel);
			if(!result) {
				writer.writeToChannel(feedback, channel);
			}
			return;
		}
		
		if(!opProcessor(cmd, feedback)) {
			System.err.println("error in processing command");
		}else {
			//buffer_source.channel.is
			writer.writeToChannel(feedback, channel);
		}
	}
	private boolean opProcessor(NIOCommand cmd, NIOCommand feedback) {
		if(cmd == null) {
			System.out.println("invalid command");
			return false;
		}
		return DirectoryController.instance.processRemoteCommand(cmd, feedback);
	}
	
	private boolean uploadCmdProcess(NIOCommand cmd, SocketChannel channel, NIOCommand feedback) {
		String flocal_path = cmd.args[0];
		String name_path = cmd.args[1];
		
		//cannot update directory until actual file data finish uploading
		//pending for the file
		//first we still need to know if the directory is valid
		String fname = CommandParsingHelper.getNamefromPath(flocal_path);
		DFile pending_f = DirectoryController.instance.createFilePre(fname, name_path);
		if(pending_f == null) {
			feedback.args[0] = "The path is invalid";
			return false;
		}
		
		//load balance
		List<DataNodeAddress> file_addrs = new ArrayList<DataNodeAddress>();
		List<DataNodeAddress> addresses = NameNodeServer.server.dataAddresses;
		DataNodeAddress node1 = addresses.get(0);
		DataNodeAddress node2 = addresses.get(1);
		if(node1 != null) {
			file_addrs.add(node1);
		}
		if(node2 != null) {
			file_addrs.add(node2);
		}
		pending_files.put(pending_f.id, new DFileReceive(pending_f,2));
		
		//send NIOCommand back to client
		SendFileObject sfo = new SendFileObject(pending_f.id, flocal_path,file_addrs);
		NIOCommand send_client = NIOCommandFactory.commandSendFile(sfo);
		writer.writeToChannel(send_client, channel);
		return true;
		
	}
	
	private boolean downloadCmdProcess(NIOCommand cmd, NIOCommand feedback) {
		String fname_path = cmd.args[0];
		String dest_path = cmd.args[1];
		DFile file = DirectoryController.instance.findFile(fname_path);
		if(file == null) {
			feedback.args[0] = "the file in remote directory is not found";
			return false;
		}
		for(DataNodeAddress dna: file.containedNodes) {
			if(server.dataNodeChannels.containsKey(dna)) {
				//it is currently alive
				SocketChannel channel = server.dataNodeChannels.get(dna);
				//send NIOCommand to datanode
				SendFileObject sfo = new SendFileObject(file.id, dest_path, 
						new InetSocketAddress(cmd.args[2], Integer.parseInt(cmd.args[3])),file.name);
				NIOCommand send_datanode = NIOCommandFactory.commandSendFile(sfo);
				writer.writeToChannel(send_datanode, channel);
			}
		}
		
		return true;
	}
	
	private boolean receiveFeedCmdProcess(NIOCommand cmd, NIOCommand feedback, SocketChannel channel) {
		try {
			//first check the info is from valid datanode server source
			InetSocketAddress addr = new InetSocketAddress(cmd.args[1], Integer.parseInt(cmd.args[2]));
			DataNodeAddress dna = server.containsNodeAddress(addr);
			if(dna == null) {
				feedback.args[0] = "the datanode does not contain such address " + addr.toString();
				return false;
			}
			//second check the file is in pending_files sequence
			UUID id = UUID.fromString(cmd.args[0]);
			if(!pending_files.containsKey(id)) {
				//?Maybe I should do sth here to remove the unintended file
				feedback.args[0] = "the file received at datanode is not expected";
				return false;
			}
			
			DFileReceive fro = pending_files.get(id);
			DFile file = fro.file;
			file.containedNodes.add(dna);
			if(fro.first_to_receive) {
				file.parentDir.containedFiles.put(file.name, file);
				fro.first_to_receive = false;
			}
			//update times of receiving
			fro.left_to_receive -= 1;
			if(fro.left_to_receive == 0) {
				pending_files.remove(id);
			}
			
		} catch (Exception e) {
			feedback.args[0] = "the exception occurs during execution";
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

}

