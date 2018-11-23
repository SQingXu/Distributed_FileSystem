package directory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nio.DataNodeAddress;

public class DFile extends NameDirFileObject{
	//at NameNode
	public DirectoryAbst parentDir;
	public String name;
	public final UUID id;
	public List<DataNodeAddress> containedNodes;
	public DFile(String name, DirectoryAbst parent, List<DataNodeAddress> nodes) {
		id = UUID.randomUUID();
		this.name = name;
		this.parentDir = parent;
		this.isFile = true;
		//copy the list
		containedNodes = new ArrayList<>();
		for(DataNodeAddress addr: nodes) {
			containedNodes.add(addr);
		}
		
	}
	
	
	
	
}
