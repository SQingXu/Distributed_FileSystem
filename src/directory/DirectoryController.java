package directory;

import java.util.TreeMap;
import java.util.UUID;


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
    	directories.put(root_dir.id, root_dir);
    }
    
	@Override
	public Directory createDir(String name) {
		DirectoryAbst parent = current_dir;
		DirectoryAbst dir = new Directory(name,parent);
		directories.put(dir.id, dir);
		//update parent directory information about sub-directories
		parent.containedDirectories.put(name, dir);
		return null;
	}
	
	@Override
	public boolean deleteDirFile(String path) {
		try {
			NameDirFileObject o = parsePath(path);
			if(!o.isFile) {
				return deleteDir((DirectoryAbst)o);
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

	private boolean deleteDir(DirectoryAbst dir) {
		try {
			if(dir.containedDirectories.size() != 0 || dir.containedFiles.size() != 0) {
				System.out.println("cannot remove non-empty directory");
				return false;
			}
			if(dir.root) {
				System.out.println("cannot remove root directory");
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
	public boolean moveDirFile(String dir_str, String new_path) {
		NameDirFileObject o;
		DirectoryAbst parent;
		try {
			o = parsePath(dir_str);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		
		try {
			parent = parseDirPath(new_path);
		}catch(Exception e) {
			System.out.println("invalid destination");
			return false;
		}
		
		if(o.isFile) {
			return moveFile((DFile)o,parent);
		}else {
			return moveDir((DirectoryAbst)o,parent);
		}
	}
	
	private boolean moveFile(DFile file, DirectoryAbst parent) {
		file.parentDir.containedFiles.remove(file.name);
		parent.containedFiles.put(file.name, file);
		return true;
	}
	
	private boolean moveDir(DirectoryAbst dir, DirectoryAbst parent) {
		
		if(dir.root) {
			System.out.println("cannot move root directory");
			return false;
		}
		if(DirOneContainsDirTwo(dir, parent)) {
			System.out.println("cause cycles");
			return false;
		}
		dir.parentDir.containedDirectories.remove(dir.name);
		dir.parentDir = parent;
		parent.containedDirectories.put(dir.name, dir);
		return true;
	}
	
	public boolean setCurrentDir(String path) {
		try {
			DirectoryAbst dir = parseDirPath(path);
			current_dir = dir;
			return true;
		}catch(Exception e) {
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
	public boolean renameDirFile(String path, String name) {
		NameDirFileObject o;
		try {
			o = parsePath(path);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		if(o.isFile) {
			return renameFile((DFile)o, name);
		}else {
			return renameDir((DirectoryAbst)o, name);
		}
	}
	
	private boolean renameFile(DFile file, String name) {
		file.name = name;
		return true;
	}

	
	private boolean renameDir(DirectoryAbst dir, String name) {
		if(dir.root) {
			System.out.println("cannot rename root dir");
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
			createDir(cmd.args[0]);
		}else if(cmd.type == NIOCommandType.REMOVE_DIR_FILE) {
			//arg1: dir to remove
			return deleteDirFile(cmd.args[0]);
		}else if(cmd.type == NIOCommandType.MOVE_DIR_FILE) {
			//arg1: dir to move
			//arg2: destination
			return this.moveDirFile(cmd.args[0], cmd.args[1]);
		}else if(cmd.type == NIOCommandType.RENAME_DIR_FILE) {
			//arg1: dir to rename
			//arg2: name
			return this.renameDirFile(cmd.args[0], cmd.args[1]);
		}else if(cmd.type == NIOCommandType.SET_CURRENT_DIRECTORY){
			return this.setCurrentDir(cmd.args[0]);
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
