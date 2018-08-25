package nio;

import java.net.InetSocketAddress;
import java.util.UUID;

public class NIOCommandHeaderSendFileFromDataNode extends NIOCommandHeader{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public UUID file_id;
	public InetSocketAddress client_address;
	public String file_name;
	public NIOCommandHeaderSendFileFromDataNode(UUID file_id, InetSocketAddress address, String file_name) {
		super();
		this.file_id = file_id;
		this.client_address = address;
		this.file_name = file_name;
	}

}
