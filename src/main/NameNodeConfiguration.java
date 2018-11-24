package main;

import java.util.List;

import nio.DataNodeAddress;

public class NameNodeConfiguration {
	public String backup_dir = "/Users/davidxu/Desktop/Java";
	public int replication_number = 2;
	public int changeThreshold = 1;
	public List<DataNodeAddress> dataAddresses;
	
	public NameNodeConfiguration() {
	}
	
	public void loadFromFile(String xmlfile) {
		
	}
}
