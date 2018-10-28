package directory;

import java.util.ArrayList;
import java.util.List;

public class FileController implements FileControllerI{
	public List<FileTransactionRecord> transactionRecords;
	public static FileController instance = new FileController();
	private DirectoryController dirSingleton = DirectoryController.instance;
	private FileController() {
		transactionRecords = new ArrayList<FileTransactionRecord>();
	}

	@Override
	public boolean createFile(String name) {
		DFile file = new DFile(name, dirSingleton.current_dir);
		dirSingleton.current_dir.containedFiles.put(name, file);
		return true;
	}

	@Override
	public boolean createFile(String path, String name) {
		NameDirFileObject dfo;
		try {
			dfo = dirSingleton.parsePath(path);
			if(dfo.isFile) {
				System.out.println("the path is a file but not a directory");
				return false;
			}
		}catch(Exception e) {
			return false;
		}
		DirectoryAbst dir = (DirectoryAbst)dfo;
		DFile file = new DFile(name, dir);
		dir.containedFiles.put(name, file);
		return true;
	}

	@Override
	public boolean deleteFile(String name) {
		boolean res = dirSingleton.current_dir.containedFiles.containsKey(name);
		if(!res) return false;
		dirSingleton.current_dir.containedFiles.remove(name);
		return true;
	}

	@Override
	public boolean deleteFile(String path, String name) {
		NameDirFileObject dfo;
		try {
			dfo = dirSingleton.parsePath(path);
			if(dfo.isFile) {
				System.out.println("the path is a file but not a directory");
				return false;
			}
		}catch(Exception e) {
			return false;
		}
		DirectoryAbst dir = (DirectoryAbst)dfo;
		boolean res = dir.containedFiles.containsKey(name);
		if(!res) return false;
		dir.containedFiles.remove(name);
		return true;
	}

}
