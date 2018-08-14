package directory;
import java.util.HashMap;

public class Directory extends DirectoryAbst implements DirectoryI {
	
	public Directory(String name, DirectoryAbst parent, int code) {
		super(code);
		this.name = name;
		this.parentDir = parent;
		containedFiles = new HashMap<>();
		containedDirectories = new HashMap<>();
	}
	
	@Override
	public boolean addFile(DFile file) {
		containedFiles.put(file.name, file);
		return true;
	}
	@Override
	public boolean addDirectory(Directory dir) {
		containedDirectories.put(dir.code, dir);
		return false;
	}
	
	

}
