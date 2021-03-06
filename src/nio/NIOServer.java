package nio;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOServer implements Runnable{
	public static NIOServer server = new NIOServer();
	public ServerSocketChannel serverChannel;
	public SocketChannel sendClientChannel;
	public Selector selector;
	public InetSocketAddress clientAddress = new InetSocketAddress("localhost",1002);
	private NIOServer() {
		
	}
	
	public void init(String host, int port) {
		try {
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(host, port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			
			int ops = serverChannel.validOps();
			
			sendClientChannel = SocketChannel.open(clientAddress);
			
			SelectionKey ky = serverChannel.register(selector, ops, null);
			
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println("server socket initilization error");
		}
		
	}
	
	public void syncSelect() {
		
	}

	@Override
	public void run() {
		while(true) {
			try {
				selector.select();
				for(SelectionKey key: selector.selectedKeys()) {
					if(key.isAcceptable()) {
						SocketChannel clientChannel = serverChannel.accept();
						clientChannel.configureBlocking(false);
						clientChannel.register(selector, SelectionKey.OP_READ);
						
					}else if(key.isConnectable()) {
					
						
					}else if(key.isReadable()) {
						
					}
				}
			}catch(Exception e) {
				
			}
			
		}
		
	}
	
}
