import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

import nio.ByteBufferHeaderInfo;
import nio.ByteBufferWSource;
import nio.CommandReaderFileServer;
import nio.FileHeaderEncodingHelper;
import nio.NIOCommandHeaderReceiveFile;
import nio.NIOSerializer;
import nio.NIOServerNameNode;

public class TestNIOServer implements Runnable{
	public static TestNIOServer server = new TestNIOServer();
	public ServerSocketChannel serverChannel;
	public InetSocketAddress[] datanode_addresses;
	public Selector selector;
	public ByteBuffer buffer;
	public CommandReaderFileServer reader;
	
	public Thread read_thread;
	public Thread select_thread;
	
	boolean first_packet = true;
	ByteBufferHeaderInfo header_info = null;
	String header_str = "";
	
	public TestNIOServer() {
		
	}
	
	public void init(String host, int port) {
		try {
			//first start reading thread
			reader = new CommandReaderFileServer(1024);
			read_thread = new Thread(reader);
			read_thread.start();
			
			selector = Selector.open();
			buffer = ByteBuffer.allocate(1024);
			
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(host, port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);

			int ops = serverChannel.validOps();
			
			serverChannel.register(selector, ops, null);
			System.out.println("server channel initialized successfully");
			
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println("server socket initilization error");
		}
	}
	
	public void syncSelect() {
		select_thread = new Thread(server);
		select_thread.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				//System.out.println("start select");
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while(iterator.hasNext()) {
					SelectionKey key = iterator.next();
					if(!key.isValid()) {
						System.err.println("invalid keys");
					}else if(key.isAcceptable()) {
						SocketChannel clientChannel = serverChannel.accept();
						clientChannel.configureBlocking(false);
						clientChannel.register(selector, SelectionKey.OP_READ);
						Socket info = clientChannel.socket();
						
						System.out.println("New Connected Channel: ");
						System.out.println(info.getRemoteSocketAddress().toString());
						
					}else if(key.isReadable()) {
						System.out.println("the key becomes readable");
						//file server 
						//first read a commandMsg object
						SocketChannel channel = (SocketChannel)key.channel();
						int num_bytes = channel.read(buffer);
						
						if(num_bytes == -1) {
							//reach EOF
							channel.close();
							continue;
						}
						System.out.println("number of bytes read: " + num_bytes);
						//flip and duplicate
						buffer.flip();
						ByteBuffer mBuffer = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());;
						byte[] anArrayCopy = Arrays.copyOfRange(mBuffer.array(), mBuffer.position(), mBuffer.limit());
						ByteBuffer dBuffer = ByteBuffer.wrap(anArrayCopy,0, mBuffer.remaining());
						
						ByteBufferWSource dBufferSource = new ByteBufferWSource(dBuffer,channel);
						reader.addBufferWSource(dBufferSource);
//						if(first_packet) {
//							header_info = FileHeaderEncodingHelper.getHeaderLength(dBuffer);
//							System.out.println("header_length: " + header_info.length + " remaining start index: " +
//							header_info.start + " overhead: " + header_info.start);
//							header_str = new String(dBuffer.array(), header_info.start,
//									Math.min(header_info.length, dBuffer.remaining()-header_info.start));
//							header_info.length = header_info.length - dBuffer.remaining() + header_info.start;
//							first_packet = false;
//						}else {
//							if(header_info.length > 0) {
//								header_str += new String(dBuffer.array(),0,
//										Math.min(header_info.length, dBuffer.remaining()));
//								header_info.length -= dBuffer.remaining();
//							}
//						}
						
//						if(header_info.length <= 0) {
//							NIOCommandHeaderReceiveFile cmdH = (NIOCommandHeaderReceiveFile)NIOSerializer.FromString(header_str);
//							System.out.println("file_id: " + cmdH.file_id + " file_name: " + cmdH.file_name);
//						}
						
						
						//reader.addBufferWSource(dBufferSource);
						
						//buffer.remaining() = length
						buffer.clear();
						
					}
					iterator.remove();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		server.init("localhost", 10000);
		server.syncSelect();
		
	}

}
