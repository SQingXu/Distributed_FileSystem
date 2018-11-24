package loadbalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nio.DataNodeAddress;
import nio.NameNodeServer;

public class LoadBalance{
	public Map<DataNodeAddress, Long> balanceStatus;
	public NameNodeServer server;
	public LoadBalance(NameNodeServer server) {
		this.server = server;
		balanceStatus = new HashMap<>();
		for(DataNodeAddress dna: server.dataAddresses) {
			balanceStatus.put(dna, (long) 0);
		}
	}
	
	public void addToBalance(DataNodeAddress dna, long file_size) {
		if(!balanceStatus.containsKey(dna)) {
			return;
		}
		balanceStatus.put(dna, Math.min(balanceStatus.get(dna) + file_size, Long.MAX_VALUE/balanceStatus.size()));
	}
	
	public void substractToBalance(DataNodeAddress dna, long file_size) {
		if(!balanceStatus.containsKey(dna)) {
			return;
		}
		balanceStatus.put(dna, Math.max(balanceStatus.get(dna) - file_size, 0));
	}
	
	public List<DataNodeAddress> getNodes(){
		List<DataNodeAddress> alived = new ArrayList<>();
		for(DataNodeAddress dna:server.dataNodeChannels.keySet()) {
			alived.add(dna);
		}
		int replication_number = Math.min(server.replication_number, alived.size());
		List<DataNodeAddress> res = new ArrayList<>();
		if(alived.size() == replication_number) {
			return alived;
		}
		for(int i = 0; i < replication_number; i++) {
			res.add(getNextNode(alived));
		}
		return res;
	}
	
	public String balanceStatusStr() {
		long total = 0;
		for(DataNodeAddress dna:balanceStatus.keySet()) {
			total += balanceStatus.get(dna);
		}
		String ret = "total bytes: " + total;
		for(DataNodeAddress dna:balanceStatus.keySet()) {
			ret += "\n";
			long val = balanceStatus.get(dna);
			float part = 0.0f;
			if(total != 0) {
				part = ((float)val)/((float)total);
			}
			String part_str = String.format("%.3f", part);
			ret += "id: " + dna.getId() + "; address: " + dna.getServerAddress().toString() + "; load: " + part_str;
		}
		return ret;
	}
	
	private DataNodeAddress getNextNode(List<DataNodeAddress> alived) {
		Random rand = new Random();
		float r = rand.nextFloat();
		long[] running_sums = new long[alived.size()];
		long running_sum = 0;
		for(int i = 0; i < alived.size(); i++) {
			running_sum += balanceStatus.get(alived.get(i));
			running_sums[i] = running_sum;
		}
		long r_val = (long)(r * running_sum);
		for(int i = 0; i < running_sums.length; i++) {
			if(r_val <= running_sums[i]) {
				alived.remove(i);
				return alived.get(i);
			}
		}
		//should not run to here
		System.err.println("something goes wrong in load balance algorithm");
		return null;
	}
	
	
}
