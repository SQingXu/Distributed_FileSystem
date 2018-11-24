package nio;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.UUID;

import niocmd.NIOCommand;
import niocmd.NIOCommandFactory;
import niocmd.ReceiveFileObject;
import niocmd.SendFileObject;

public class NIOFileSendingTask implements Runnable{
	public InetSocketAddress address;
	public SocketChannel sendChannel;
	public RandomAccessFile aFile;
	public SendFileObject sfo;
	public FileServer server;
	
	public NIOFileSendingTask(SendFileObject sfo, InetSocketAddress address, FileServer server) {
		this.address = address;
		this.sfo = sfo;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			
			sendChannel = SocketChannel.open(address);
			
			//blocking mode if channel is connected then move to next step
			File file;
			if(!server.datanode) {
				file = new File(sfo.file_path);
			}else {
				file = new File(server.dns.data_dir + "/"+ sfo.file_id.toString());
			}
			
			aFile = new RandomAccessFile(file, "r");
			FileChannel inChannel = aFile.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			//first send a command header
			
			ReceiveFileObject rfo = new ReceiveFileObject(sfo.file_name,sfo.file_id, sfo.file_path);
			NIOCommand header = NIOCommandFactory.commandReceiveFile(rfo);
			
			String header_str = FileHeaderEncodingHelper.addLengthHeader(NIOSerializer.toString(header));
			ByteBuffer header_buffer = ByteBuffer.wrap(header_str.getBytes());
			int header_length = sendChannel.write(header_buffer);
			//System.out.println("send header data packet: " + header_length);
			//System.out.println("file_id: " + sfo.file_id + " file_name: " + sfo.file_name);
			int result = inChannel.read(buffer);
			while(result > 0) {
				buffer.flip();
				while(buffer.remaining() > 0) {
					sendChannel.write(buffer);
				}
				buffer.clear();
				result = inChannel.read(buffer);
			}
			//End of file
			System.out.println("file " + sfo.file_path + " end of file sending reached");
			sendChannel.close();
			aFile.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
