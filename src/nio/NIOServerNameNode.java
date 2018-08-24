package nio;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOServerNameNode implements Runnable{
	public static NIOServerNameNode server = new NIOServerNameNode();
	public ServerSocketChannel serverChannel;
	public InetSocketAddress[] datanode_addresses;
	public Selector selector;
	protected NIOServerNameNode() {
		
	}
	
	public void init(String host, int port) {
		try {
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
		Thread select = new Thread(server);
		select.start();
	}
	
	private ByteBuffer processRead(ByteBuffer buffer) {
		buffer.flip();
		ByteBuffer ret = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());
		return ret;
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
						ByteBuffer commandHeaderBuffer = processRead(buffer);
						//buffer.remaining() = length
						buffer.clear();
						
					}
				}
			}catch(Exception e) {
				
			}
			
		}
		
	}
	
}

