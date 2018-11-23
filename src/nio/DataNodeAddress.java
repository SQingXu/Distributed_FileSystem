package nio;

import java.net.InetSocketAddress;

public class DataNodeAddress {
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
}
