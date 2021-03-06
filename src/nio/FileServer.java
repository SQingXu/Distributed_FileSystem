package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

import directory.DataNodeStructure;

public class FileServer implements Runnable {
	public static FileServer server = new FileServer();
	public boolean datanode;
	public DataNodeStructure dns;
	public InetSocketAddress namenode_address;
	public Selector selector;
	public ServerSocketChannel serverChannel;
	public SocketChannel nameChannel;
	public boolean connected = false;
	ByteBuffer buffer;
	
	public String data_host = "";
	public int data_name_port = 0;
	
	public FileServerReader reader;
	public ServerWriter writer;
	
	
	public Thread write_thread;
	public Thread read_thread;
	public Thread select_thread;
	
	protected FileServer() {
		dns = new DataNodeStructure("");
	}
	
	public void init(String host, int port, int name_port) {
		try {
			//first start writer and reader thread
			writer = new ServerWriter(1024);
			write_thread = new Thread(writer);
			write_thread.start(); 
			
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
			if(datanode) {
				this.data_host = host;
				this.data_name_port = name_port;
				nameChannel.bind(new InetSocketAddress(host, name_port));
			}
			nameChannel.configureBlocking(false);
			connected = nameChannel.connect(namenode_address);
			if(connected) {
				System.out.println("successfully connected to namenode in first attempt");
				nameChannel.register(selector, SelectionKey.OP_READ);
			}else {
				System.out.println("connected at later attempt");
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
					if(!key.channel().isOpen()) {
						key.cancel();
						continue;
					}
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
						//System.out.println("the key is readable");
						//file server 
						//first read a commandMsg object
						SocketChannel channel = (SocketChannel)key.channel();
						
						int num_bytes = channel.read(buffer);
						if(num_bytes < 0) {
							System.out.println("the channel is closed");
							reader.channelClosed(channel);
							channel.close();
							if(channel.equals(nameChannel)) {
								System.out.println("name channel lost connection, need reconnection");
								repeatedConnect();
							}
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
						try {
							connected = nameChannel.finishConnect();
						}catch(Exception e) {
							//wait for another time
							Thread.sleep(5000);
							repeatedConnect();
							continue;
						}
						System.out.println("finishConnect operation ends");
						if(!connected) {
							System.err.println("error in connecting to namenode");
							repeatedConnect();
						}else {
							key.interestOps(SelectionKey.OP_READ);
						}
					}else if(key.isWritable()) {
					}
					iterator.remove();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void repeatedConnect() {
		//if the exception occurs continue connecting attempts
		//if the the connected if false due to first connect attempt, jump out
		connected = false;
		while(!connected) {
			try {
				//reopen the channel
				nameChannel = SocketChannel.open();
				nameChannel.configureBlocking(false);
				if(datanode) {
					nameChannel.bind(new InetSocketAddress(data_host, data_name_port));
				}
				connected = nameChannel.connect(namenode_address);
			} catch (IOException e) {
				System.out.println("exception occurs " + e.getClass().getName());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					continue;
				}
				continue;
			}
			if(connected) {
				System.out.println("successfully connected to namenode in first attempt");
				try {
					nameChannel.register(selector, SelectionKey.OP_READ);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
					connected = false;
					continue;
				}
			}else {
				System.out.println("connected at later attempt");
				try {
					nameChannel.register(selector, SelectionKey.OP_CONNECT);
					break;
				} catch (ClosedChannelException e) {
					e.printStackTrace();
					continue;
				}
			}
			
		}
		
	}
	
	

}
