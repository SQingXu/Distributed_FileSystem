package directory;

import java.util.List;
import nio.DataNodeAddress;
import niocmd.NIOCommand;

public interface DirectoryControllerI {
	public boolean createDir(String name, NIOCommand feedback);
	public DFile createFilePre(String fname, String path, NIOCommand feedback, long size);
	public DFile findFile(String fpath);
	public boolean createFile(String fname, String path, List<DataNodeAddress> nodes, long size);
	public boolean deleteDirFile(String path, NIOCommand feedback);
	public boolean moveDirFile(String dir_str, String new_path, NIOCommand feedback);
	public String currentPath();
	public boolean renameDirFile(String path, String name, NIOCommand feedback);
	public boolean processRemoteCommand(NIOCommand cmd, NIOCommand feedback);
	public boolean setCurrentDir(String path, NIOCommand feedback);
}
