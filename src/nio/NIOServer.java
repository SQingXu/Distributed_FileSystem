package nio;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class NIOServer {
	public static NIOServer server = new NIOServer();
	public ServerSocketChannel serverChannel;
	public Selector selector;
	private NIOServer() {
		
	}
	
	public void init(String host, int port) {
		try {
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(host, port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			
			
		}catch(Exception e) {
			
		}
		
	}
}
