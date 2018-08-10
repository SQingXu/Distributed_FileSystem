package directory;

public interface DirectoryControllerI {
	public Directory createDir(boolean root, Directory parent);
	public boolean deleteDir(String name);
	public boolean moveDir(String name);
	
	
}
