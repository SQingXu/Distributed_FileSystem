package directory;

import niocmd.NIOCommand;

public interface DirectoryControllerI {
	public Directory createDir(String name);
	public boolean deleteDirFile(String path);
	public boolean moveDirFile(String dir_str, String new_path);
	public String currentPath();
	public boolean renameDirFile(String path, String name);
	public boolean processRemoteCommand(NIOCommand cmd);
}
