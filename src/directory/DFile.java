package directory;

import java.util.UUID;

public class DFile extends NameDirFileObject{
	//at NameNode
	public DirectoryAbst parentDir;
	public String name;
	public final UUID id;
	public DFile(String name, DirectoryAbst parent) {
		id = UUID.randomUUID();
		this.name = name;
		this.parentDir = parent;
		this.isFile = true;
	}
	
	
}
