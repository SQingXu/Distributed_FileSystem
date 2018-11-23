package niocmd;
import java.io.Serializable;
import java.util.UUID;

public class ReceiveFileObject implements Serializable {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String file_name;
	public UUID file_id;
	public String file_path = "";
	public ReceiveFileObject(String name, UUID file_id) {
		this.file_name = name;
		this.file_id = file_id;
	}
	//for downloading
	public ReceiveFileObject(String name, UUID file_id, String path) {
		this.file_name = name;
		this.file_id = file_id;
		this.file_path = path;
	}
}
