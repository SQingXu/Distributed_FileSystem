package nio;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.UUID;

public class NIOFileSendingTask implements Runnable{
	public String local_path;
	public InetSocketAddress address;
	public SocketChannel sendChannel;
	public RandomAccessFile aFile;
	public UUID file_id;
	public String file_name;
	
	public NIOFileSendingTask(String file_path, UUID file_id, InetSocketAddress address, String file_name) {
		this.local_path = file_path;
		this.address = address;
		this.file_id = file_id;
		this.file_name = file_name;
	}

	@Override
	public void run() {
		try {
			
			sendChannel = SocketChannel.open(address);
			
			//blocking mode if channel is connected then move to next step
			File file = new File(local_path);
			aFile = new RandomAccessFile(file, "r");
			FileChannel inChannel = aFile.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			//first send a command header
			NIOCommandHeaderReceiveFile header;
			header = new NIOCommandHeaderReceiveFile(file_name,file_id);
			
			String header_str = FileHeaderEncodingHelper.addLengthHeader(NIOSerializer.toString(header));
			ByteBuffer header_buffer = ByteBuffer.wrap(header_str.getBytes());
			int header_length = sendChannel.write(header_buffer);
			System.out.println("send header data packet: " + header_length);
			System.out.println("file_id: " + header.file_id + " file_name: " + header.file_name);
			
			while(inChannel.read(buffer) > 0) {
				buffer.flip();
				int length = sendChannel.write(buffer);
				System.out.println("send one data packet: " + length);
				buffer.clear();
			}
			//End of file
			System.out.println("file " + local_path + " end of file sending reached");
			System.out.println("Closing");
			sendChannel.close();
			aFile.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
