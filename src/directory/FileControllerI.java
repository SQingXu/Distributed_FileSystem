package directory;

public interface FileControllerI {
	public boolean createFile(String name);
	public boolean createFile(String path, String name);
	public boolean deleteFile(String name);
	public boolean deleteFile(String path, String name);
	
	
}
