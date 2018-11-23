import java.net.InetSocketAddress;
import java.util.Scanner;

import main.CommandParsingHelper;
import main.InvalidCommandException;
import nio.ClientFileServer;
import nio.FileServer;
import niocmd.NIOCommand;

public class ClientMain {
	public static void main(String[] args) {
		FileServer server = FileServer.server;
		server.datanode = false;
		server.namenode_address = new InetSocketAddress("localhost", 10000);
		server.init("localhost", 10005, 0000);
		server.syncSelect();
		//take input here
		Scanner input = new Scanner(System.in);
		while(true) {
			try {
				String cmd_str = input.nextLine();
				if(cmd_str == "") {
					continue;
				}
				NIOCommand cmd  = CommandParsingHelper.parseCmdString(cmd_str);
				server.writer.writeToChannel(cmd, server.nameChannel);
			} catch (InvalidCommandException e) {
				// TODO Auto-generated catch block
				System.out.println("invalid command");
				continue;
			}
			
			
		}
	}
}
