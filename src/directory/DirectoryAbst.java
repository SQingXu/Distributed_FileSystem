package directory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class DirectoryAbst implements DirectoryI, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean root = false;
	public String name;
	public DirectoryAbst parentDir;
	public final UUID id; //using int code to index directories
	public Map<String, DFile> containedFiles;
	public Map<String, DirectoryAbst> containedDirectories;
	public DirectoryAbst() {
		this.id = UUID.randomUUID();
		this.containedFiles = new HashMap<String, DFile>();
		this.containedDirectories = new HashMap<String, DirectoryAbst>();
	}
	
}
