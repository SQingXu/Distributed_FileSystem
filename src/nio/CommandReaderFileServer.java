package nio;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommandReaderFileServer implements Runnable{
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> receivingBufferQueues;
	public BlockingQueue<ByteBuffer> nameQueue;
	public int queue_size;
	public FileServer server;
	public CommandReaderFileServer(FileServer server, int queue_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
		this.server = server;
	}
	public CommandReaderFileServer(int queue_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
	}
	
	
	public void addBufferWSource(ByteBufferWSource bufferSource) throws InterruptedException{
		SocketChannel channel = bufferSource.channel;
//		if(bufferSource.channel.equals(server.nameChannel)) {
//			nameQueue.add(bufferSource.buffer);
//			return;
//		}
		if(!receivingBufferQueues.containsKey(channel)) {
			receivingBufferQueues.put(channel, new ArrayBlockingQueue<ByteBuffer>(queue_size));
			NIOFileReceivingTask task = new NIOFileReceivingTask(receivingBufferQueues, channel, bufferSource.buffer);
			Thread receiving = new Thread(task);
			receiving.start();
		}else {
			receivingBufferQueues.get(channel).put(bufferSource.buffer);
		}
		
		
	}
	
	@Override
	public void run() {
		while(true) {
			if(!nameQueue.isEmpty()) {
				try {
					ByteBuffer buffer = nameQueue.take();
					String command_str = new String(buffer.array(),buffer.position(), buffer.remaining());
					NIOCommandHeader cmdH = (NIOCommandHeader)NIOSerializer.FromString(command_str);
					cmdProcessor(cmdH);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean cmdProcessor(NIOCommandHeader cmdH) {
		if(cmdH instanceof NIOCommandHeaderSendFileFromClient) {
			
		}else if(cmdH instanceof NIOCommandHeaderSendFileFromDataNode) {
			
		}else {
			return false;
		}
		return true;
	}
}
