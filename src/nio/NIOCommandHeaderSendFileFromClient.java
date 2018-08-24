package nio;

import java.net.InetSocketAddress;
import java.util.UUID;

public class NIOCommandHeaderSendFileFromClient extends NIOCommandHeader{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String file_path;
	public UUID file_id;
	public InetSocketAddress[] node_addresses;
	public NIOCommandHeaderSendFileFromClient(String file_path, UUID id,InetSocketAddress[] node_addresses) {
		this.file_path = file_path;
		this.node_addresses = node_addresses;
		this.file_id = id;
		
	}
	
}