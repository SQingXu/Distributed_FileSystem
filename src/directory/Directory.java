package directory;

public class Directory extends DirectoryAbst implements DirectoryI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Directory(String name, DirectoryAbst parent) {
		super();
		this.name = name;
		this.parentDir = parent;
	}
	

}
