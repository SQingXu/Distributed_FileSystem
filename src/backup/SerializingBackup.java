package backup;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import directory.DirectoryAbst;
import directory.DirectoryController;
import nio.NIOSerializer;

public class SerializingBackup implements Runnable{
	public String backup_dir;
	public static int changedObjects = 0;
	public int changeThreshold;
	String rootfile = "directory_data";
	public SerializingBackup(String dir, int threshold) {
		this.backup_dir = dir;
		this.changeThreshold = threshold;
	}
	//This class is solely for backup data on the namenode
	public boolean SerializeNameNode() {
		String root_str;
		RandomAccessFile file;
		try {
			root_str = NIOSerializer.toString(DirectoryController.instance.root_dir);
			file = new RandomAccessFile(backup_dir + "/" + rootfile, "rw");
			file.write(root_str.getBytes());
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean DeserializeFromFile() {
		String path = backup_dir + "/" + rootfile;
		try {
			String root_str = new String(Files.readAllBytes(Paths.get(path)));
			Object root_o = NIOSerializer.FromString(root_str);
			if(!(root_o instanceof DirectoryAbst)) {
				return false;
			}
			File file = new File(path);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			System.out.println("backup last modified time: " + sdf.format(file.lastModified()));
			
			DirectoryAbst root_dir = (DirectoryAbst)root_o;
			DirectoryController.instance.root_dir = root_dir;
			DirectoryController.instance.current_dir = root_dir;
		} catch (Exception e) {
			return false;
		}
		return true;
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
