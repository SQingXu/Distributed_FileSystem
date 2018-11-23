package nio;

public class DataNodeFileServer extends FileServer{
	public static DataNodeFileServer server = new DataNodeFileServer("/");
	protected DataNodeFileServer(String file_dir) {
		super();
		this.datanode = true;
	}
}
