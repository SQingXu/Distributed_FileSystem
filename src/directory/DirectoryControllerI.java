package directory;

import nio.NIOCommandHeaderDirOp;

public interface DirectoryControllerI {
	public Directory createDir(String name);
	public boolean deleteDir(String path);
	public boolean moveDir(String dir_str, String new_path);
	public String currentPath();
	public boolean renameDir(String path, String name);
	public boolean processRemoteCommand(NIOCommandHeaderDirOp cmdH);
}
