package nio;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import directory.DirectoryController;
import niocmd.NIOCommand;

public class NameNodeServerReader implements Runnable{
	public BlockingQueue<ByteBufferWSource> buffer_queue;
	public ServerWriter writer;
	public NameNodeServerReader(int queue_size, ServerWriter writer) {
		buffer_queue = new ArrayBlockingQueue<>(queue_size);
		this.writer = writer;
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
					String command_str = new String(buffer.array(),buffer.position(), buffer.remaining());
					NIOCommand cmd = (NIOCommand)NIOSerializer.FromString(command_str);
					if(!opProcessor(cmd)) {
						System.err.println("error in processing command");
					}else {
						//buffer_source.channel.is
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private boolean opProcessor(NIOCommand cmd) {
		if(cmd == null) {
			System.out.println("invalid command");
			return false;
		}
		return DirectoryController.instance.processRemoteCommand(cmd);
	}
	

}

