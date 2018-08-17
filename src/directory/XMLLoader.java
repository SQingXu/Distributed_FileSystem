package directory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XMLLoader {
	public XMLLoader singleton = new XMLLoader();
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	TransformerFactory tfFactory = TransformerFactory.newInstance();
	DocumentBuilder dBuilder;
	Transformer transformer;
	
	private XMLLoader() {
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			transformer = tfFactory.newTransformer();
		}catch(Exception e){
			System.err.println("XMLLoader initialization failed");
			e.printStackTrace();
		}
	}
	
	DirectoryAbst loadDirectoryFromFile(String path, int name_code) {
		DirectoryAbst directory = null;
		try {
			File inputFile = new File(path);
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			Element rootEle = doc.getDocumentElement();
			boolean isRoot = (Integer.parseInt(rootEle.getAttribute("IsRoot")) == 0)?false:true;
			int code = Integer.parseInt(rootEle.getAttribute("Code"));
			int parent_code = Integer.parseInt(rootEle.getAttribute("Parent"));
			String name = rootEle.getAttribute("Name");
			if(isRoot) {
				directory = DirectoryRoot.rootDir;
			}else {
				DirectoryAbst parent = loadDirectoryFromFile(path, parent_code);
				//TODO find directory
				directory = new Directory(name, parent);
			}
			
			
			
			
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		DebugPrintDirectory(directory);
		return directory;
	}
	
	boolean saveDirectoryToFile(String path, Directory directory, int name_code) {
		String file_name = Integer.toString(name_code) + ".xml";
		Path file_path = Paths.get(path, file_name);
		File input_file = new File(file_path.toString());
		//TODO: check if the file exist either create or update
		Document doc = dBuilder.newDocument();
		
		//root element directory
		Element rootEle = doc.createElement("Directory");
		doc.appendChild(rootEle);
		
		//directory attributes
		Attr isroot = doc.createAttribute("IsRoot");
		int root_val = (directory.root)?1:0;
		isroot.setValue(Integer.toString(root_val));
		rootEle.setAttributeNode(isroot);
		
		Attr name = doc.createAttribute("Name");
		name.setValue(directory.name);
		rootEle.setAttributeNode(name);
		
		Attr code = doc.createAttribute("Code");
		code.setValue(directory.id.toString());
		rootEle.setAttributeNode(code);
		
		if(!directory.root) {
			Attr parent_code = doc.createAttribute("Parent");
			parent_code.setValue(directory.parentDir.id.toString());
			rootEle.setAttributeNode(parent_code);
		}
		
		//directory subdirs
		Element subdirsEle = doc.createElement("SubDirectories");
		rootEle.appendChild(subdirsEle);
		Attr dirssize = doc.createAttribute("Size");
		dirssize.setValue(Integer.toString(directory.containedDirectories.size()));
		subdirsEle.setAttributeNode(dirssize);
		for(String dir_code:directory.containedDirectories.keySet()) {
			Element dirEle = doc.createElement("SubDirectory");
			subdirsEle.appendChild(dirEle);
			Attr sub_code = doc.createAttribute("Code");
			sub_code.setValue(dir_code);
			dirEle.setAttributeNode(sub_code);
		}
		
		//directory files
		Element subfilesEle = doc.createElement("SubFiles");
		rootEle.appendChild(subfilesEle);
		Attr filessize = doc.createAttribute("Size");
		filessize.setValue(Integer.toString(directory.containedFiles.size()));
		subfilesEle.setAttributeNode(filessize);
		for(String subfile_name:directory.containedFiles.keySet()) {
			Element fileEle = doc.createElement("SubFile");
			subfilesEle.appendChild(fileEle);
			Attr sub_filename = doc.createAttribute("Name");
			sub_filename.setValue(subfile_name);
			fileEle.setAttributeNode(sub_filename);
			//TODO in case files object are added with any identification variables
		}
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(input_file);
		try {
			transformer.transform(source, result);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return true;
	}
	
	public void DebugPrintDirectory(DirectoryAbst directory) {
		System.out.println("IsRoot: " + directory.root);
		System.out.println("Code: " + directory.id.toString());
		if(directory instanceof DirectoryRoot) {
		}else {
			System.out.println("Parent: " + directory.parentDir.id.toString());
		}
		System.out.println("SubDirectories: ");
		for(String code: directory.containedDirectories.keySet()) {
			System.out.println(code);
		}
		
		System.out.println("Files: ");
		for(String filename: directory.containedFiles.keySet()) {
			System.out.println(filename);
		}
		
	}
}
