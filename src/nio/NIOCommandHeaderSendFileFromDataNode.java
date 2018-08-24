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
	public NIOCommandHeaderSendFileFromDataNode(UUID file_id, InetSocketAddress address) {
		super();
		this.file_id = file_id;
		this.client_address = address;
	}

}
