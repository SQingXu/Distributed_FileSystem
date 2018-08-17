import java.util.TreeMap;
import java.util.UUID;

import directory.DirectoryController;

public class TestMain {
	public static void main(String[] args) {
		UUID id  = UUID.randomUUID();
		String id_str = id.toString();
		TreeMap<UUID, Integer> tm = new TreeMap<>();
		tm.put(id,2);
		System.out.println(tm.get(UUID.fromString(id_str)));
		DirectoryController singleton = DirectoryController.instance;
		DirectoryController singleton2 = DirectoryController.instance;
		singleton.createDir("dir1");
		singleton.createDir("dir2");
		singleton.setCurrentDir("/dir1");
		
		singleton.createDir("dir3");
		System.out.println(singleton.currentPath());
		singleton.moveDir("dir3", "/dir2/");
		singleton.setCurrentDir("../dir2/dir3");
		System.out.println(singleton.currentPath());
		singleton.deleteDir(".");
		System.out.println(singleton.currentPath());
	}
}
