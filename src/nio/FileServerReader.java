package nio;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import niocmd.NIOCommand;
import niocmd.NIOCommandFactory;
import niocmd.NIOCommandType;
import niocmd.SendFileObject;

public class FileServerReader implements Runnable, ReceivingListener{
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> receivingBufferQueues;
	public BlockingQueue<ByteBuffer> nameQueue;
	public int queue_size;
	public FileServer server;
	
	private AcumulateBuffer acu_buffer;
	
	public BlockingQueue<NIOCommand> confirmReceivingQueue;
	
	public ExecutorService threads_pool_sending;
	public ExecutorService threads_pool_receiving;
	
	public FileServerReader(FileServer server, int queue_size, int thread_pool_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
		confirmReceivingQueue = new ArrayBlockingQueue<>(queue_size);
		this.server = server;
		threads_pool_sending = Executors.newFixedThreadPool(thread_pool_size);
		threads_pool_receiving = Executors.newFixedThreadPool(thread_pool_size);
		
		
		acu_buffer = new AcumulateBuffer();
	}
//	public FileServerReader(int queue_size) {
//		receivingBufferQueues = new ConcurrentHashMap<>();
//		this.queue_size = queue_size;
//		nameQueue = new ArrayBlockingQueue<>(queue_size);
//	}
	
	
	public void addBufferWSource(ByteBufferWSource bufferSource) throws InterruptedException{
		SocketChannel channel = bufferSource.channel;
		if(bufferSource.channel.equals(server.nameChannel)) {
			//System.out.println("add to nameChannel");
			nameQueue.add(bufferSource.buffer);
			return;
		}
		if(!receivingBufferQueues.containsKey(channel)) {
			receivingBufferQueues.put(channel, new ArrayBlockingQueue<ByteBuffer>(queue_size));
			NIOFileReceivingTask task = 
					new NIOFileReceivingTask(receivingBufferQueues, channel, bufferSource.buffer,server.datanode, this, server.dns);
			//Thread receiving = new Thread(task);
			//receiving.start();
			threads_pool_receiving.execute(task);
		}else {
			receivingBufferQueues.get(channel).put(bufferSource.buffer);
		}
		
		
	}
	
	public void channelClosed(SocketChannel channel) {
		if(channel.equals(server.nameChannel)) {
			//do nothing for now
		}
		if(receivingBufferQueues.containsKey(channel)) {
			//worst case senario the channel accidentally closed during the file transferring
			receivingBufferQueues.remove(channel);
		}
	}
	
	@Override
	public void run() {
		while(true) {
			if(!nameQueue.isEmpty()) {
				try {
					ByteBuffer buffer = nameQueue.take();
					while(buffer.remaining() > 0) {
						if(acu_buffer.acumulate(buffer)) {
							processCmd(acu_buffer.gatheredBuffer);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			if(!confirmReceivingQueue.isEmpty()) {
				try {
					NIOCommand cmd = this.confirmReceivingQueue.take();
					server.writer.writeToChannel(cmd, server.nameChannel);
				}catch(Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	private void processCmd(ByteBuffer buffer) {
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
		if(!cmdProcessor(cmd)) {
			System.err.println("error in processing command");
		}
	}
	
	private boolean cmdProcessor(NIOCommand cmd) {
		//System.out.println("Command type is: " + cmd.type.toString());
		if(cmd.type.equals(NIOCommandType.SEND_FILE_DATA)) {
			SendFileObject sfo = NIOCommandFactory.fromCmdSendFile(cmd);
			for(InetSocketAddress address: sfo.node_addresses) {
				//send file to each of listed datanodes/client
				if(server.datanode) {
					//consider the case when the datanode does not contain the requested file
					File file_test = new File(server.dns.data_dir + "/"+ sfo.file_id.toString());
					if(!file_test.exists()) {
						String[] args = new String[1];
						args[0] = sfo.nfile_path;
						NIOCommand error = new NIOCommand(NIOCommandType.NOTCONTAIN_FILE_FEED,args);
						server.writer.writeToChannel(error, server.nameChannel);
					}
				}
				NIOFileSendingTask task = new NIOFileSendingTask(sfo, address, server);
				threads_pool_sending.execute(task);
			}
		}else if(cmd.type.equals(NIOCommandType.RESULT_FEED)) {
			//print out the feedback
			if(cmd.args[0] != null || !cmd.args[0].equals("")) {
				System.out.println(cmd.args[0]);
			}
		}else if(cmd.type.equals(NIOCommandType.REMOVE_FILE_DATA)) {
			if(server.datanode) {
				String id_str  = cmd.args[0];
				File file = new File(server.dns.data_dir + "/" + id_str);
				boolean res = file.delete();
				res = !file.exists();
				if(res) {
					server.dns.containedFiles.remove(UUID.fromString(id_str));
					//send remove feed back to namenode
					NIOCommand feed = new NIOCommand(NIOCommandType.REMOVE_FILE_FEED, cmd.args);
					server.writer.writeToChannel(feed, server.nameChannel);
				}
			}else {
				//do nothing
			}
			
		}
		return true;
	}


	@Override
	public void notifyReceived(UUID id) {
		String[] args = new String[3];
		try {
			args[0] = id.toString();
			InetSocketAddress server_addr = (InetSocketAddress)server.serverChannel.getLocalAddress();
			args[1] = server_addr.getHostName();
			args[2] = Integer.toString(server_addr.getPort());
			NIOCommand cmd = new NIOCommand(NIOCommandType.RECEIVE_DATA_FEED, args);
			confirmReceivingQueue.put(cmd);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
