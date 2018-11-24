package directory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import nio.DataNodeAddress;
import niocmd.NIOCommandType;
import niocmd.NIOCommand;

public class DirectoryController implements DirectoryControllerI{
	//implement red black tree stored in memory to store directory structure <String id, directory object>
    TreeMap<UUID, DirectoryAbst> directories;
    public DirectoryAbst root_dir;
    public DirectoryAbst current_dir;
    
    //singleton
    public static DirectoryController instance = new DirectoryController();
    private DirectoryController() {
    	System.out.println("create directory singleton");
    	directories = new TreeMap<UUID, DirectoryAbst>();
    	//first put default root directory into tree structure
    	root_dir = DirectoryRoot.rootDir;
    	current_dir = root_dir;
    }
    
	@Override
	public boolean createDir(String name, NIOCommand feedback) {
		DirectoryAbst parent = current_dir;
		//first to check if the directory name already exist in current directory
		if(parent.containedDirectories.containsKey(name)|| parent.containedFiles.containsKey(name)) {
			feedback.args[0] = "mkdir error: cannot create repeated name directory";
			return false;
		}
		DirectoryAbst dir = new Directory(name,parent);
		//update parent directory information about sub-directories
		parent.containedDirectories.put(name, dir);
		return true;
	}
	
	@Override
	public boolean createFile(String fname, String path, List<DataNodeAddress> nodes) {
		NameDirFileObject o;
		try {
			o = parsePath(path);
		} catch (InValidPathException e) {
			e.printStackTrace();
			return false;
		}
		DFile file = new DFile(fname, (DirectoryAbst)o, nodes);
		((DirectoryAbst)o).containedFiles.put(fname, file);
		return true;
	}
	
	@Override
	public DFile createFilePre(String name, String path, NIOCommand feedback) {
		NameDirFileObject o;
		try {
			o = parsePath(path);
		} catch (InValidPathException e) {
			feedback.args[0] = "upload error: invalid path";
			return null;
		}
		if(o.isFile) {
			feedback.args[0] = "upload error: invalid path (it is a file)";
			return null;
		}
		if(((DirectoryAbst)o).containedDirectories.containsKey(name) || ((DirectoryAbst)o).containedFiles.containsKey(name)) {
			feedback.args[0] = "upload error: repeated name";
			return null;
		}
		DFile file = new DFile(name, (DirectoryAbst)o, new ArrayList<>());
		return file;
	}
	
	@Override
	public DFile findFile(String fpath) {
		NameDirFileObject o;
		try {
			o = parsePath(fpath);
		} catch (InValidPathException e) {
			e.printStackTrace();
			return null;
		}
		if(!o.isFile) {
			return null;
		}else {
			return (DFile)o;
		}
	}
	
	
	@Override
	public boolean deleteDirFile(String path, NIOCommand feedback) {
		try {
			NameDirFileObject o = parsePath(path);
			if(!o.isFile) {
				return deleteDir((DirectoryAbst)o, feedback);
			}else {
				return deleteFile((DFile)o);
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean deleteFile(DFile file) {
		file.parentDir.containedFiles.remove(file.name);
		return true;
	}

	private boolean deleteDir(DirectoryAbst dir, NIOCommand feedback) {
		try {
			if(dir.containedDirectories.size() != 0 || dir.containedFiles.size() != 0) {
				feedback.args[0] = "remove error: cannot remove non-empty directory";
				return false;
			}
			if(dir.root) {
				feedback.args[0] = "remove error: cannot remove root directory";
				return false;
			}
			//remove from tree and parent directory update current directory
			if(DirOneContainsDirTwo(dir,current_dir)) {
				current_dir = dir.parentDir;
			}
			directories.remove(dir.id);
			dir.parentDir.containedDirectories.remove(dir.name);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean moveDirFile(String dir_str, String new_path, NIOCommand feedback) {
		NameDirFileObject o;
		DirectoryAbst parent;
		try {
			o = parsePath(dir_str);
		}catch(Exception e) {
			feedback.args[0] = "move error: the directory/file you are trying to move is invalid";
			return false;
		}
		
		
		try {
			parent = parseDirPath(new_path);
		}catch(Exception e) {
			feedback.args[0] = "move error: the destination directory is invalid";
			return false;
		}
		
		if(o.isFile) {
			return moveFile((DFile)o,parent, feedback);
		}else {
			return moveDir((DirectoryAbst)o,parent, feedback);
		}
	}
	
	private boolean moveFile(DFile file, DirectoryAbst parent, NIOCommand feedback) {
		if(parent.containedDirectories.containsKey(file.name) || parent.containedFiles.containsKey(file.name)) {
			feedback.args[0] = "move error: destination directory has repeated name file/directory";
			return false;
		}
		file.parentDir.containedFiles.remove(file.name);
		parent.containedFiles.put(file.name, file);
		return true;
	}
	
	private boolean moveDir(DirectoryAbst dir, DirectoryAbst parent, NIOCommand feedback) {
		
		if(dir.root) {
			feedback.args[0] = "move error: cannot move root directory";
			return false;
		}
		if(DirOneContainsDirTwo(dir, parent)) {
			feedback.args[0] = "move error: move operation causes cycles";
			return false;
		}
		if(parent.containedDirectories.containsKey(dir.name) || parent.containedFiles.containsKey(dir.name)) {
			feedback.args[0] = "move error: destination directory has repeated name file/directory";
			return false;
		}
		dir.parentDir.containedDirectories.remove(dir.name);
		dir.parentDir = parent;
		parent.containedDirectories.put(dir.name, dir);
		return true;
	}
	
	@Override
	public boolean setCurrentDir(String path, NIOCommand feedback) {
		try {
			DirectoryAbst dir = parseDirPath(path);
			current_dir = dir;
			return true;
		}catch(Exception e) {
			feedback.args[0] = e.getMessage();
			return false;
		}
		
	}
	
	
	public DirectoryAbst parseDirPath(String path) throws InValidPathException{
		NameDirFileObject ret = parsePath(path);
		if(ret.isFile) {
			throw new InValidPathException("the path point to a file not a directory");
		}
		return (DirectoryAbst)ret;
	}
	
	
	public NameDirFileObject parsePath(String path) throws InValidPathException {
		NameDirFileObject dir;
		int start = 0;
		int end = 0; 
		if(path.charAt(0) == '/') {
			//start from root directory
			dir = root_dir;
			start = 1;
		}else {
			dir = current_dir;
		}
		for(int i = start; i < path.length(); i++) {
			end = i;
			if(path.charAt(i) == '/') {
				String name = path.substring(start, end);;
				start = i+1;
				dir = parseOneDirectory((DirectoryAbst)dir,name);
				if(dir.isFile) {
					throw new InValidPathException(name + " is a file not a directory");
				}
			}
		}
		if(start < end) {
			String name = path.substring(start);
			dir = parseOneDirectory((DirectoryAbst)dir, name);
		}
		return dir;
	}
	
	private NameDirFileObject parseOneDirectory(DirectoryAbst dir, String name) throws InValidPathException {
		NameDirFileObject ret = null;
		if(name.equals(".")) {
			ret = dir;
		}else if(name.equals("..")) {
			//go to parent directory
			if(dir.root) {
				throw new InValidPathException();
			}
			ret = dir.parentDir;
		}else {
			ret = dir.containedDirectories.get(name);
			if(ret == null) {
				ret = dir.containedFiles.get(name);
				if(ret == null) {
					throw new InValidPathException();
				}
			}
		}
		return ret;
	}
	
	private boolean DirOneContainsDirTwo(DirectoryAbst dir1, DirectoryAbst dir2) {
		while(dir2 != null) {
			if(dir1.id.equals(dir2.id)) {
				return true;
			}
			dir2 = dir2.parentDir;
		}
		return false;
	}

	@Override
	public String currentPath() {
		DirectoryAbst dir = current_dir;
		String ret = "";
		while(!dir.root) {
			ret = dir.name + "/" + ret;
			dir = dir.parentDir;
		}
		ret = "/"+ret;
		return ret;
	}
	
	@Override
	public boolean renameDirFile(String path, String name, NIOCommand feedback) {
		NameDirFileObject o;
		try {
			o = parsePath(path);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		if(o.isFile) {
			return renameFile((DFile)o, name, feedback);
		}else {
			return renameDir((DirectoryAbst)o, name, feedback);
		}
	}
	
	private boolean renameFile(DFile file, String name, NIOCommand feedback) {
		if(file.parentDir.containedFiles.containsKey(name) || 
				file.parentDir.containedDirectories.containsKey(name)) {
			//repeated file name in same directory
			feedback.args[0] = "rename error: repeated name in same directory";
			return false;
		}
		file.name = name;
		return true;
	}

	
	private boolean renameDir(DirectoryAbst dir, String name, NIOCommand feedback) {
		if(dir.root) {
			feedback.args[0] = "rename error: cannot rename root directory";
			return false; 
		}
		if(dir.parentDir.containedFiles.containsKey(name) || 
				dir.parentDir.containedDirectories.containsKey(name)) {
			//repeated file name in same directory
			feedback.args[0] = "rename error: repeated name in same directory";
			return false;
		}
		
		dir.parentDir.containedDirectories.remove(dir.name);
		dir.name = name;
		dir.parentDir.containedDirectories.put(name, dir);
		return true;
	}

	@Override
	public boolean processRemoteCommand(NIOCommand cmd, NIOCommand feedback) {
		System.out.println("Command type is: " +  cmd.type.toString());
		if(cmd.type == NIOCommandType.CREATE_DIR) {
			//arg1: dir name 
			createDir(cmd.args[0], feedback);
		}else if(cmd.type == NIOCommandType.REMOVE_DIR_FILE) {
			//arg1: dir to remove
			return deleteDirFile(cmd.args[0], feedback);
		}else if(cmd.type == NIOCommandType.MOVE_DIR_FILE) {
			//arg1: dir to move
			//arg2: destination
			return moveDirFile(cmd.args[0], cmd.args[1], feedback);
		}else if(cmd.type == NIOCommandType.RENAME_DIR_FILE) {
			//arg1: dir to rename
			//arg2: name
			return this.renameDirFile(cmd.args[0], cmd.args[1], feedback);
		}else if(cmd.type == NIOCommandType.SET_CURRENT_DIRECTORY){
			return this.setCurrentDir(cmd.args[0], feedback);
		}else if(cmd.type == NIOCommandType.PRINT_WORKING_DIRECTORY){
			System.out.println(this.currentPath());
			if(feedback != null) {
				feedback.type = NIOCommandType.RESULT_FEED;
				feedback.args = new String[1];
				feedback.args[0] = currentPath();
			}
			return true;
		}else if(cmd.type == NIOCommandType.LIST_WORKING_DIRECTORY) {
			String ret = "";
			for(String dir_name: this.current_dir.containedDirectories.keySet()) {
				ret += dir_name + '\n';
			}
			for(String file_name: this.current_dir.containedFiles.keySet()) {
				ret += file_name + '\n';
			}
			if(feedback != null) {
				feedback.type = NIOCommandType.RESULT_FEED;
				feedback.args = new String[1];
				feedback.args[0] = ret;
			}
		}else {
			return false;
		}
		return true;
	}
	
	
	
	
	

}
