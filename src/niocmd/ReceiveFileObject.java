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
	public ReceiveFileObject(String name, UUID file_id) {
		this.file_name = name;
		this.file_id = file_id;
	}
}
