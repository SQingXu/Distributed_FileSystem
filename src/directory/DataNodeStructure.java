package directory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.UUID;

public class DataNodeStructure implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String data_dir;
	public HashSet<UUID> containedFiles;
	
	public DataNodeStructure(String data_dir) {
		this.data_dir = data_dir;
		this.containedFiles = new HashSet<>();
	}
	
	public void addFile(UUID id) {
		containedFiles.add(id);
	}
	
}

