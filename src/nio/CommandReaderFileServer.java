package nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandReaderFileServer implements Runnable{
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> receivingBufferQueues;
	public BlockingQueue<ByteBuffer> nameQueue;
	public int queue_size;
	public FileServer server;
	
	public ExecutorService threads_pool_sending;
	public ExecutorService threads_pool_receiving;
	
	public CommandReaderFileServer(FileServer server, int queue_size, int thread_pool_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
		this.server = server;
		threads_pool_sending = Executors.newFixedThreadPool(thread_pool_size);
		threads_pool_receiving = Executors.newFixedThreadPool(thread_pool_size);
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
			NIOFileReceivingTask task = 
					new NIOFileReceivingTask(receivingBufferQueues, channel, bufferSource.buffer,server.datanode);
			//Thread receiving = new Thread(task);
			//receiving.start();
			threads_pool_receiving.execute(task);
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
			if(server.datanode) {
				return false;
			}
			for(InetSocketAddress address: ((NIOCommandHeaderSendFileFromClient)cmdH).node_addresses) {
				//send file to each of listed datanodes
				NIOFileSendingTask task = new NIOFileSendingTask(((NIOCommandHeaderSendFileFromClient)cmdH).file_path,
						((NIOCommandHeaderSendFileFromClient)cmdH).file_id, 
						address,
						((NIOCommandHeaderSendFileFromClient)cmdH).file_name);
				threads_pool_sending.execute(task);
			}
		}else if(cmdH instanceof NIOCommandHeaderSendFileFromDataNode) {
			String file_path = server.file_dir + ((NIOCommandHeaderSendFileFromDataNode)cmdH).file_id.toString();
			NIOFileSendingTask task = new NIOFileSendingTask(file_path,
					((NIOCommandHeaderSendFileFromDataNode)cmdH).file_id,
					((NIOCommandHeaderSendFileFromDataNode)cmdH).client_address,
					((NIOCommandHeaderSendFileFromDataNode)cmdH).file_name);
			threads_pool_sending.execute(task);
		}else {
			return false;
		}
		return true;
	}
}
