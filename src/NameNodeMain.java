import nio.NameNodeServer;

public class NameNodeMain {
	public static void main(String[] args) {
		NameNodeServer server = NameNodeServer.server;
		server.init("localhost", 10000);
		server.syncSelect();
	}
}
