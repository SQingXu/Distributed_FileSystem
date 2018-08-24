package nio;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class NIOFileReceivingTask implements Runnable{
	public BlockingQueue<ByteBuffer> queue;
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> receivingBufferQueues;
	public SocketChannel receiveChannel;
	public boolean readMeta = false;
	public ByteBuffer meta_buffer;
	public RandomAccessFile aFile;
	public ByteBufferHeaderInfo header_info;
	public String header_str;
	
	public NIOFileReceivingTask(ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> bufferQueues,
			SocketChannel channel, ByteBuffer meta) {
		this.receivingBufferQueues = bufferQueues;
		this.queue = bufferQueues.get(channel);
		this.receiveChannel = channel;
		this.meta_buffer = meta;
	}
	
	@Override
	public void run() {
		try {
			//first meta data
			header_info = FileHeaderEncodingHelper.getHeaderLength(meta_buffer);
			System.out.println("header_length: " + header_info.length + " remaining start index: " +
					header_info.start + " overhead: " + header_info.start);
			header_str = new String(meta_buffer.array(), header_info.start,
					Math.min(header_info.length, meta_buffer.remaining()-header_info.start));
			header_info.length = header_info.length - meta_buffer.remaining() + header_info.start;
			while(header_info.length > 0) {
				meta_buffer = queue.take();
				header_str += new String(meta_buffer.array(),0,
						Math.min(header_info.length, meta_buffer.remaining()));
				header_info.length -= meta_buffer.remaining();
			}
			int start_pos = (header_str.length()+header_info.start)% meta_buffer.remaining();
			ByteBuffer remaining_buffer = ByteBuffer.wrap(meta_buffer.array(),
					start_pos, meta_buffer.remaining()-start_pos);
			
			NIOCommandHeader cmdH = (NIOCommandHeader)NIOSerializer.FromString(header_str);
			if(!(cmdH instanceof NIOCommandHeaderReceiveFile)) {
				return;
			}
			aFile = new RandomAccessFile(((NIOCommandHeaderReceiveFile)cmdH).file_id.toString(), "rw");
			FileChannel fileChannel = aFile.getChannel();
			
			fileChannel.write(remaining_buffer);
			while(!queue.isEmpty() || receiveChannel.isOpen()) {
				fileChannel.write(queue.take());
			}
			fileChannel.close();
			System.out.println("finish receiving file: " + ((NIOCommandHeaderReceiveFile)cmdH).file_name);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
