package directory;

public class DirectoryRoot extends Directory implements DirectoryI{
	public static DirectoryRoot rootDir = new DirectoryRoot("root",null, 0);
	public int total_number_directories; //for indexing directories
	
	private DirectoryRoot(String name, Directory parent, int code) {
		super(name, parent, code);
		this.root = true;
		total_number_directories = 1;
	}
	
	public void directoryCreated() {
		total_number_directories++;
	}
	
	public void directoryRemoved() {
		total_number_directories--;
	}
	

}
