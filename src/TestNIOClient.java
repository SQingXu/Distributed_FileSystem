import java.net.InetSocketAddress;
import java.util.UUID;

import nio.NIOFileSendingTask;

public class TestNIOClient {
	static NIOFileSendingTask sendingTask;
	public static void main(String[] args) {
		InetSocketAddress address = new InetSocketAddress("localhost",10000);
		UUID id = UUID.randomUUID();
		String local_path = "/Users/davidxu/Desktop/PS_David_Xu_R1.docx";
		sendingTask = new NIOFileSendingTask(local_path, id, address);
		Thread sendThread = new Thread(sendingTask);
		sendThread.start();
	}
}
