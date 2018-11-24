package nio;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;

public class DataNodeAddress implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private InetSocketAddress server_address;
	private InetSocketAddress namechannel_address;
	public DataNodeAddress(int id, InetSocketAddress ad, int name_port) {
		this.id = id;
		this.server_address = ad;
		this.namechannel_address = new InetSocketAddress(ad.getHostName(),name_port);
	}
	public InetSocketAddress getServerAddress() {
		return server_address;
	}
	public InetSocketAddress getNameConnectedAddress() {
		return namechannel_address;
	}
	public int getId() {
		return id;
	}
	
	public void printAddress() {
		System.out.println("id: " + id + ";\nholding server at: " + server_address.toString() 
		+ "\nconnect to namenode via: " + namechannel_address.toString());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof DataNodeAddress)) {
			return false;
		}
		DataNodeAddress other = (DataNodeAddress)o;
		if(other.id == id && 
				other.server_address.equals(server_address) && 
				other.namechannel_address.equals(namechannel_address)) {
			return true;
		}
		return false;
	}
	@Override 
	public int hashCode() {
		return Objects.hash(this.id, this.server_address, this.namechannel_address);
	}
}
