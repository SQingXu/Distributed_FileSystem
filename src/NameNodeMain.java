import java.net.InetSocketAddress;

import nio.DataNodeAddress;
import nio.NameNodeServer;

public class NameNodeMain {
	public static void main(String[] args) {
		NameNodeServer server = NameNodeServer.server;
		server.init("localhost", 10000);
		
		//hard-coded address
		InetSocketAddress address1 = new InetSocketAddress("localhost", 10003);
		InetSocketAddress address2 = new InetSocketAddress("localhost", 10004);
		server.dataAddresses.add(new DataNodeAddress(1, address1, 10006));
		server.dataAddresses.add(new DataNodeAddress(2, address2, 10007));
		
		server.syncSelect();
		
		
	}
}
