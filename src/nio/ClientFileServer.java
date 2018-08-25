package nio;

public class ClientFileServer extends FileServer{
	public static ClientFileServer server = new ClientFileServer();
	protected ClientFileServer() {
		super();
		this.datanode = false;
	}

}
