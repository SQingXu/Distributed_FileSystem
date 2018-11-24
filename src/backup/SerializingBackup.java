package backup;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import directory.DirectoryAbst;
import directory.DirectoryController;
import loadbalance.LoadBalance;
import nio.NIOSerializer;
import nio.NameNodeServer;
import niocmd.NIOCommandType;

public class SerializingBackup implements Runnable{
	public String backup_dir;
	public static int changedObjects = 0;
	public int changeThreshold;
	
	public NameNodeServer server;
	
	String rootfile = "directory_data";
	String balancefile = "balance_data";
	public SerializingBackup(String dir, int threshold, NameNodeServer server) {
		this.backup_dir = dir;
		this.changeThreshold = threshold;
		this.server = server;
	}
	//This class is solely for backup data on the namenode
	public boolean SerializeNameNode() {
		try {
			//root_directory
			serializeOneObject(DirectoryController.instance.root_dir, rootfile);
			//load_balance
			serializeOneObject((HashMap)server.loadBalanceStatus.balanceStatus, balancefile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	private void serializeOneObject(Serializable o, String name) throws Exception{
		String o_str = NIOSerializer.toString(o);
		//first delete previous backup
		File f = new File(backup_dir + "/" + name);
		if(f.exists()) {
			f.delete();
		}
		
		RandomAccessFile file = new RandomAccessFile(backup_dir + "/" + name, "rw");
		file.write(o_str.getBytes());
		file.close();
	}
	
	public boolean DeserializeFromFile() {
		try {
			//root directory
			Object root_o = deserializeOneFile(rootfile);
			if(!(root_o instanceof DirectoryAbst)) {
				return false;
			}
			DirectoryAbst root_dir = (DirectoryAbst)root_o;
			DirectoryController.instance.root_dir = root_dir;
			DirectoryController.instance.current_dir = root_dir;
			
			//load balance
			Object balance_o = deserializeOneFile(balancefile);
			if(!(balance_o instanceof HashMap)) {
				System.out.println("it is not a instance of HashMap");
				return false;
			}
			server.loadBalanceStatus.balanceStatus = (Map)balance_o;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private Object deserializeOneFile(String name) throws Exception{
		String path = backup_dir + "/" + name;
		File file = new File(path);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		System.out.println(name + " backup last modified time: " + sdf.format(file.lastModified()));
		String o_str = new String(Files.readAllBytes(Paths.get(path)));
		Object o = NIOSerializer.FromString(o_str);
		return o;
	}
	
	public static boolean stateChangeOperation(NIOCommandType type) {
		if(type.equals(NIOCommandType.CREATE_DIR) 
				|| type.equals(NIOCommandType.MOVE_DIR_FILE)
				|| type.equals(NIOCommandType.RECEIVE_DATA_FEED) 
				|| type.equals(NIOCommandType.REMOVE_FILE_FEED)
				|| type.equals(NIOCommandType.RENAME_DIR_FILE)
				|| type.equals(NIOCommandType.NOTCONTAIN_FILE_FEED)) {
			return true;
		}
		return false;
	}
	
	public static synchronized void stateChange(boolean reset) {
		if(reset) {
			changedObjects = 0;
		}else {
			changedObjects++;
		}
		
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			if(changedObjects >= changeThreshold) {
				System.out.println("the state change over threshold, starting backup");
				this.SerializeNameNode();
				SerializingBackup.stateChange(true);
			}
			
		}
	}
	
}
