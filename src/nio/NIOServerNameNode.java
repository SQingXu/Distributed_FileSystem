package nio;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class NIOServerNameNode implements Runnable{
	public static NIOServerNameNode server = new NIOServerNameNode();
	public ServerSocketChannel serverChannel;
	public InetSocketAddress[] datanode_addresses;
	public Selector selector;
	public CommandReaderNameNode reader;
	
	public Thread read_thread;
	public Thread select_thread;
	
	protected NIOServerNameNode() {
		
	}
	
	public void init(String host, int port) {
		try {
			reader = new CommandReaderNameNode(1024);
			read_thread = new Thread(reader);
			read_thread.start();
			
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(host, port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			
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
	

	@Override
	public void run() {
		while(true) {
			try {
				selector.select();
				for(SelectionKey key: selector.selectedKeys()) {
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
						//first read a commandMsg object
						SocketChannel channel = (SocketChannel)key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						channel.read(buffer);
						buffer.flip();
						ByteBuffer ret = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());
						
						ByteBuffer mBuffer = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());;
						byte[] anArrayCopy = Arrays.copyOfRange(mBuffer.array(), mBuffer.position(), mBuffer.limit());
						ByteBuffer dBuffer = ByteBuffer.wrap(anArrayCopy,0, mBuffer.remaining());
						
						ByteBufferWSource dBufferSource = new ByteBufferWSource(dBuffer,channel);
						reader.addBufferWSource(dBufferSource);
						//buffer.remaining() = length
						buffer.clear();
						
					}
				}
			}catch(Exception e) {
				
			}
			
		}
		
	}
	
}

