package directory;

import java.util.List;
import nio.DataNodeAddress;
import niocmd.NIOCommand;

public interface DirectoryControllerI {
	public Directory createDir(String name);
	public DFile createFilePre(String fname, String path);
	public DFile findFile(String fpath);
	public boolean createFile(String fname, String path, List<DataNodeAddress> nodes);
	public boolean deleteDirFile(String path);
	public boolean moveDirFile(String dir_str, String new_path);
	public String currentPath();
	public boolean renameDirFile(String path, String name);
	public boolean processRemoteCommand(NIOCommand cmd, NIOCommand feedback);
}
