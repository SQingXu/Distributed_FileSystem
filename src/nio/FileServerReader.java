package nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import niocmd.NIOCommand;
import niocmd.NIOCommandFactory;
import niocmd.NIOCommandType;
import niocmd.SendFileObject;

public class FileServerReader implements Runnable{
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> receivingBufferQueues;
	public BlockingQueue<ByteBuffer> nameQueue;
	public int queue_size;
	public FileServer server;
	
	public ExecutorService threads_pool_sending;
	public ExecutorService threads_pool_receiving;
	
	public FileServerReader(FileServer server, int queue_size, int thread_pool_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
		this.server = server;
		threads_pool_sending = Executors.newFixedThreadPool(thread_pool_size);
		threads_pool_receiving = Executors.newFixedThreadPool(thread_pool_size);
	}
	public FileServerReader(int queue_size) {
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
					//TODO: the buffer contains a length header and 
					// we should also consider what if the buffer is incomplete
					String command_str = new String(buffer.array(),buffer.position(), buffer.remaining());
					NIOCommand cmd = (NIOCommand)NIOSerializer.FromString(command_str);
					
					cmdProcessor(cmd);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean cmdProcessor(NIOCommand cmd) {
		if(cmd.type.equals(NIOCommandType.SEND_FILE_DATA)) {
			SendFileObject sfo = NIOCommandFactory.fromCmdSendFile(cmd);
			for(InetSocketAddress address: sfo.node_addresses) {
				//send file to each of listed datanodes
				NIOFileSendingTask task = new NIOFileSendingTask(sfo, address);
				threads_pool_sending.execute(task);
			}
		}else if(cmd.type.equals(NIOCommandType.RESULT_FEED)) {
			//print out the feedback
			System.out.println(cmd.args[0]);
		}
		return true;
	}
}
