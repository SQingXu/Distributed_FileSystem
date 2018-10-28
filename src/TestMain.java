
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;

import directory.DirectoryController;
import main.CommandParsingHelper;
import nio.FileHeaderEncodingHelper;

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
		
		String test_str = "dhcweiqhocqjiwjf/efqi53jeooqeowd--+wdjfqpe~`";
		System.out.println("String length: " + test_str.length());
		byte[] test_bytes = test_str.getBytes();
		System.out.println("byte length: " + test_bytes.length);
		ByteBuffer test_buffer = ByteBuffer.wrap(test_str.getBytes());
		System.out.println("buffer remaining: " + test_buffer.remaining() + " buffer position: " + test_buffer.position() + 
				" buffer limit: " + test_buffer.limit() + " buffer capacity: " + test_buffer.capacity());
		
		ByteBuffer sub_buffer1 = ByteBuffer.wrap(test_buffer.array(),0,3);
		ByteBuffer sub_buffer2 = ByteBuffer.wrap(test_buffer.array(),3, test_buffer.remaining()-3);
		String sub_str1 = new String(sub_buffer1.array(),sub_buffer1.position(),sub_buffer1.remaining());
		String sub_str2 = new String(sub_buffer2.array(),sub_buffer2.position(),sub_buffer2.remaining());
		System.out.println("Sub String1: " + sub_str1 + " Sub String2: " + sub_str2);
		
		System.out.println(String.valueOf(91));
		test_str = FileHeaderEncodingHelper.addLengthHeader(test_str);
		test_buffer = ByteBuffer.wrap(test_str.getBytes());
		System.out.println(FileHeaderEncodingHelper.getHeaderLength(test_buffer));
		
		List<String> result = CommandParsingHelper.separateString(" cd COMP\\ 530/output.txt newnameIsRightHere   ");
		for(String str: result) {
			System.out.println(str + str.length());
		}
		//test create file in folder with space
		String path = "/Users/davidxu/Desktop/COMP 631/output.txt";
		System.out.println(path);
		File file = new File(path);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Scanner sc = new Scanner(System.in);
		String scan_str = sc.nextLine();
		System.out.println(scan_str);
	}
	
}
