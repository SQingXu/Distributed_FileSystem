package nio;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import niocmd.NIOCommand;

public class ServerWriter implements Runnable{
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> writeBufferQueues;
	public ConcurrentHashMap<SocketChannel, Boolean> channelToWrite;
	private int write_queue_size;
	public ServerWriter(int write_queue_size) {
		writeBufferQueues = new ConcurrentHashMap<>();
		this.write_queue_size = write_queue_size;
	}
	
	public void writeToChannel(NIOCommand cmd, SocketChannel channel) {
		String str = "";
		try {
			str = NIOSerializer.toString(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteBuffer sent_buffer = ByteBufferTranslator.addHeader(str);
		if(!writeBufferQueues.containsKey(channel)) {
			writeBufferQueues.put(channel, new ArrayBlockingQueue<>(write_queue_size));
			channelToWrite.put(channel, false);
		}
		BlockingQueue<ByteBuffer> writeQueue = writeBufferQueues.get(channel);
		writeQueue.add(sent_buffer);
		return;
	}
	
	public void channelStatusUpdate(boolean val, SocketChannel channel) {
		if(!channelToWrite.containsKey(channel)) {
			return;
		}
		if(channelToWrite.get(channel).equals(val)) {
			return;
		}
		channelToWrite.put(channel, val);
		return;
	}
	@Override
	public void run() {
		while(true) {
			for(SocketChannel channel:channelToWrite.keySet()) {
				if(!channel.isOpen() || channelToWrite.get(channel)) {
					continue;
				}
				BlockingQueue<ByteBuffer> writeQueue = writeBufferQueues.get(channel);
				while(!writeQueue.isEmpty()) {
					try {
						ByteBuffer buffer = writeQueue.take();
						channel.write(buffer);
						if(buffer.remaining() > 0) {
							writeQueue.put(buffer);
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				
			}
		}
	}
}

