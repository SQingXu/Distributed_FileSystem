package directory;

import java.util.TreeMap;
import java.util.UUID;

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
	public boolean deleteDir(String path) {
		try {
			DirectoryAbst dir = parsePath(path);
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
	public boolean moveDir(String dir_str, String new_path) {
		DirectoryAbst dir;
		DirectoryAbst parent;
		try {
		    dir = parsePath(dir_str);
		}catch(Exception e) {
			System.out.println("invalid directory");
			return false;
		}
		
		try {
			parent = parsePath(new_path);
		}catch(Exception e) {
			System.out.println("invalid destination");
			return false;
		}
		
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
			DirectoryAbst dir = parsePath(path);
			current_dir = dir;
			return true;
		}catch(Exception e) {
			return false;
		}
		
	}
	
	
	public DirectoryAbst parsePath(String path) throws InValidPathException {
		DirectoryAbst dir;
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
				dir = parseOneDirectory(dir,name);
			}
		}
		if(start < end) {
			String name = path.substring(start);
			dir = parseOneDirectory(dir, name);
		}
		return dir;
	}
	
	private DirectoryAbst parseOneDirectory(DirectoryAbst dir, String name) throws InValidPathException {
		DirectoryAbst ret = null;
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
				throw new InValidPathException();
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
	public boolean renameDir(String path, String name) {
		DirectoryAbst dir;
		try {
			dir = parsePath(path);
		}catch(Exception e) {
			return false;
		}
		if(dir.root) {
			System.out.println("cannot rename root dir");
			return false; 
		}
		
		dir.parentDir.containedDirectories.remove(dir.name);
		dir.name = name;
		dir.parentDir.containedDirectories.put(name, dir);
		return true;
	}
	
	
	

}
