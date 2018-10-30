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
	
	private ByteBuffer lengthBuffer;
	private ByteBuffer gatheredBuffer;
	private final int lenBuffer_len = 4;
	private boolean getLen = false;
	
	public ExecutorService threads_pool_sending;
	public ExecutorService threads_pool_receiving;
	
	public FileServerReader(FileServer server, int queue_size, int thread_pool_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
		this.server = server;
		threads_pool_sending = Executors.newFixedThreadPool(thread_pool_size);
		threads_pool_receiving = Executors.newFixedThreadPool(thread_pool_size);
		
		lengthBuffer = ByteBuffer.allocate(lenBuffer_len);
	}
	public FileServerReader(int queue_size) {
		receivingBufferQueues = new ConcurrentHashMap<>();
		this.queue_size = queue_size;
		nameQueue = new ArrayBlockingQueue<>(queue_size);
		lengthBuffer = ByteBuffer.allocate(lenBuffer_len);
	}
	
	
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
								processCmd(gatheredBuffer);
								gatheredBuffer.clear();
							}
							buffer.position(buffer.position()+ advanced);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
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
				//send file to each of listed datanodes
				NIOFileSendingTask task = new NIOFileSendingTask(sfo, address);
				threads_pool_sending.execute(task);
			}
		}else if(cmd.type.equals(NIOCommandType.RESULT_FEED)) {
			//print out the feedback
			if(cmd.args[0] != null) {
				System.out.println(cmd.args[0]);
			}
		}
		return true;
	}
}
