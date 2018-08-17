package directory;

public class DirectoryRoot extends Directory implements DirectoryI{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static DirectoryRoot rootDir = new DirectoryRoot("root",null);
	
	private DirectoryRoot(String name, Directory parent) {
		super(name, parent);
		this.root = true;
	}
	
	

}
