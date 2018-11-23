import java.net.InetSocketAddress;

import nio.FileServer;

public class DataNodeMain2 {
	public static void main(String[] args) {
		FileServer server = FileServer.server;
		server.datanode = true;
		server.dns.data_dir = "/Users/davidxu/Desktop/Java/Datanode2";
		server.namenode_address = new InetSocketAddress("localhost", 10000);
		server.init("localhost", 10004, 10007);
		server.syncSelect();
		//take input here
//		Scanner input = new Scanner(System.in);
//		while(true) {
//			try {
//				String cmd_str = input.nextLine();
//				NIOCommand cmd  = CommandParsingHelper.parseCmdString(cmd_str);
//				server.writer.writeToChannel(cmd, server.nameChannel);
//			} catch (InvalidCommandException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				continue;
//			}
//			
//			
//		}
	}
}
