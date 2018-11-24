package niocmd;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.UUID;

import nio.DataNodeAddress;

public class SendFileObject implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String file_path = "";
	public UUID file_id = null;
	public InetSocketAddress[] node_addresses;
	public InetSocketAddress client_name_address;
	public String file_name;
	public String nfile_path = "";
	public SendFileObject(UUID id,String file_path,InetSocketAddress client_address,InetSocketAddress client_name_address , String file_name, String nfile_path) {
		//for download
		node_addresses = new InetSocketAddress[1];
		this.node_addresses[0] = client_address;
		this.client_name_address = client_name_address;
		this.file_id = id;
		this.file_name = file_name;
		this.file_path = file_path;
		this.nfile_path = nfile_path;
	}
	public SendFileObject(UUID id, String file_path, List<DataNodeAddress> addresses) {
		//for upload
		this.file_id = id;
		node_addresses = new InetSocketAddress[addresses.size()];
		int index = 0;
		for(DataNodeAddress node: addresses) {
			node_addresses[index] = node.getServerAddress();
		    index++;
		}
		this.file_path = file_path;
		
	}
}
