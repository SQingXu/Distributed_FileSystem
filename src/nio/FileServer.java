package nio;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public class FileServer implements Runnable {
	public static FileServer server = new FileServer();
	public boolean datanode;
	public InetSocketAddress namenode_address;
	public Selector selector;
	public ServerSocketChannel serverChannel;
	public SocketChannel nameChannel;
	public boolean connected = false;
	ByteBuffer buffer;
	public String file_dir = ""; //for datanode
	
	public FileServerReader reader;
	public ServerWriter writer;
	
	
	public Thread write_thread;
	public Thread read_thread;
	public Thread select_thread;
	
	protected FileServer() {
	}
	
	public void init(String host, int port) {
		try {
			//first start reading thread
			reader = new FileServerReader(server, 1024, 4);
			read_thread = new Thread(reader);
			read_thread.start();
			
			selector = Selector.open();
			buffer = ByteBuffer.allocate(1024);
			
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(host, port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			
			nameChannel = SocketChannel.open();
			nameChannel.configureBlocking(false);
			connected = nameChannel.connect(namenode_address);
			if(connected) {
				System.out.println("successfully connected to namenode in first attempt");
				nameChannel.register(selector, SelectionKey.OP_READ);
			}else {
				nameChannel.register(selector, SelectionKey.OP_CONNECT);
			}

			int ops = serverChannel.validOps();
			
			serverChannel.register(selector, ops, null);
			
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println("server socket initilization error");
		}
	}
	
	public void syncSelect() {
		select_thread = new Thread(server);
		select_thread.start();
	}
	
	public void writeToBuffer() {
		
	}
	
	@Override
	public void run() {
		while(true) {
			try {
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
						//file server 
						//first read a commandMsg object
						SocketChannel channel = (SocketChannel)key.channel();
						
						int num_bytes = channel.read(buffer);
						
						if(num_bytes <= 0 && !channel.equals(nameChannel)) {
							//file receiving channel if the receive ended, close channel
							channel.close();
							continue;
						}
						//flip and duplicate
						buffer.flip();
						ByteBuffer mBuffer = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());;
						byte[] anArrayCopy = Arrays.copyOfRange(mBuffer.array(), mBuffer.position(), mBuffer.limit());
						ByteBuffer dBuffer = ByteBuffer.wrap(anArrayCopy,0, mBuffer.remaining());
						
						ByteBufferWSource dBufferSource = new ByteBufferWSource(dBuffer,channel);
						reader.addBufferWSource(dBufferSource);
						buffer.clear();
						
					}else if(key.isConnectable()) {
						//only name channel
						connected = nameChannel.finishConnect();
						if(!connected) {
							System.err.println("error in connecting to namenode");
							break;
						}else {
							key.interestOps(SelectionKey.OP_READ);
							
						}
					}else if(key.isWritable()) {
						if(datanode) {
							
						}
					}
					iterator.remove();
				}
			}catch(Exception e) {
				
			}
			
		}
	}
	
	

}
