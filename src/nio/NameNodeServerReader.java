package nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import directory.DirectoryController;
import niocmd.NIOCommand;
import niocmd.NIOCommandType;

public class NameNodeServerReader implements Runnable{
	public BlockingQueue<ByteBufferWSource> buffer_queue;
	public ServerWriter writer;
	private ByteBuffer lengthBuffer;
	private ByteBuffer gatheredBuffer;
	private final int lenBuffer_len = 4;
	private boolean getLen = false;
	public NameNodeServerReader(int queue_size, ServerWriter writer) {
		buffer_queue = new ArrayBlockingQueue<>(queue_size);
		this.writer = writer;
		lengthBuffer = ByteBuffer.allocate(lenBuffer_len);
	}
	
	public void addBufferWSource(ByteBufferWSource buffer_source) throws InterruptedException{
		buffer_queue.put(buffer_source);
		return;
	}

	@Override
	public void run() {
		while(true) {
			if(!buffer_queue.isEmpty()) {
				try {
					ByteBufferWSource buffer_source = buffer_queue.take();
					ByteBuffer buffer = buffer_source.buffer;
					while(buffer.remaining() > 0) {
						if(!getLen) {
							if(buffer.remaining() >= lengthBuffer.remaining()) {
								int advanced = lengthBuffer.remaining();
								lengthBuffer.put(buffer.array(), buffer.position(), advanced);
								//update length buffer
								buffer.position(buffer.position()+advanced);
								lengthBuffer.flip();
								int buffer_length = lengthBuffer.getInt();
								lengthBuffer.clear();
								getLen = true;
								
								//allocate for that amount of buffer 
								gatheredBuffer = ByteBuffer.allocate(buffer_length);
							}else {
								lengthBuffer.put(buffer.array(), buffer.position(),buffer.remaining());
								buffer.position(buffer.position() + buffer.remaining());
							}
						}else {
							int advanced = Math.min(gatheredBuffer.remaining(), buffer.remaining());
							gatheredBuffer.put(buffer.array(), buffer.position(), advanced);
							if(gatheredBuffer.remaining() == 0) {
								getLen = false;
								gatheredBuffer.flip();
								processCmd(gatheredBuffer, buffer_source.channel);
								gatheredBuffer.clear();
							}
							buffer.position(buffer.position()+ advanced);
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
		System.out.println("Read string is: " + command_str);
		NIOCommand cmd;
		try {
			cmd = (NIOCommand)NIOSerializer.FromString(command_str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		NIOCommand feedback = new NIOCommand(NIOCommandType.RESULT_FEED, new String[1]);
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
	

}

