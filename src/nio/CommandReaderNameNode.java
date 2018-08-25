package nio;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import directory.DirectoryController;

public class CommandReaderNameNode implements Runnable{
	public BlockingQueue<ByteBufferWSource> buffer_queue;
	
	public CommandReaderNameNode(int queue_size) {
		buffer_queue = new ArrayBlockingQueue<>(queue_size);
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
					NIOCommandHeader cmdH = (NIOCommandHeader)NIOSerializer.FromString(command_str);
					if(!opProcessor(cmdH)) {
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
	
	private boolean opProcessor(NIOCommandHeader cmdH) {
		if(cmdH == null) {
			System.out.println("invalid command");
			return false;
		}
		if(cmdH instanceof NIOCommandHeaderDirOp) {
			return DirectoryController.instance.processRemoteCommand((NIOCommandHeaderDirOp)cmdH);
		}else{
			//unrecognized command header for namenode
			return false;
		}
	}
	

}
