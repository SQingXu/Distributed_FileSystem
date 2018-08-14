package directory;

import java.util.Map;

public abstract class DirectoryAbst implements DirectoryI{
	boolean root = false;
	public String name;
	public DirectoryAbst parentDir;
	public final int code; //using int code to index directories
	public Map<String, DFile> containedFiles;
	public Map<Integer, Directory> containedDirectories;
	public DirectoryAbst(int code) {
		this.code = code;
	}
}
