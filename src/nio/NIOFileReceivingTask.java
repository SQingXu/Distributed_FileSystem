package nio;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import directory.DataNodeStructure;
import niocmd.NIOCommand;
import niocmd.NIOCommandFactory;
import niocmd.NIOCommandType;
import niocmd.ReceiveFileObject;

public class NIOFileReceivingTask implements Runnable{
	public BlockingQueue<ByteBuffer> queue;
	public ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> receivingBufferQueues;
	public SocketChannel receiveChannel;
	public boolean readMeta = false;
	public ByteBuffer meta_buffer;
	public RandomAccessFile aFile;
	public ByteBufferHeaderInfo header_info;
	public String header_str;
	public boolean isDatanode;
	public ReceivingListener listener;
	public DataNodeStructure dns;
	
	public NIOFileReceivingTask(ConcurrentHashMap<SocketChannel, BlockingQueue<ByteBuffer>> bufferQueues,
			SocketChannel channel, ByteBuffer meta, boolean isDatanode, ReceivingListener listener, DataNodeStructure dns) {
		this.receivingBufferQueues = bufferQueues;
		this.queue = bufferQueues.get(channel);
		this.receiveChannel = channel;
		this.meta_buffer = meta;
		this.isDatanode = isDatanode;
		this.listener = listener;
		this.dns = dns;
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
			
			NIOCommand cmd = (NIOCommand)NIOSerializer.FromString(header_str);
			if(!cmd.type.equals(NIOCommandType.RECEIVE_FILE_DATA)) {
				System.out.println("received file command type incorrect");
				return;
			}
			ReceiveFileObject rfo = NIOCommandFactory.fromCmdReceiveFile(cmd); 
			if(isDatanode) {
				aFile = new RandomAccessFile(dns.data_dir + "/" + rfo.file_id.toString(), "rw");
			}else {
				aFile = new RandomAccessFile(rfo.file_path + "/" + rfo.file_name, "rw");
			}
			
			FileChannel fileChannel = aFile.getChannel();
			
			fileChannel.write(remaining_buffer);
			while(!queue.isEmpty() || receiveChannel.isOpen()) {
				fileChannel.write(queue.take());
			}
			fileChannel.close();
			dns.containedFiles.add(rfo.file_id);
			System.out.println("finish receiving file: " + rfo.file_name);
			if(isDatanode) {
				listener.notifyReceived(rfo.file_id);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			
			return;
		}
		
	}

}
