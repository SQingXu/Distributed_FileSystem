package nio;
import java.util.UUID;

public class NIOCommandHeaderReceiveFile extends NIOCommandHeader{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String file_name;
	public UUID file_id;
	public NIOCommandHeaderReceiveFile(String name, UUID file_id) {
		this.file_name = name;
		this.file_id = file_id;
	}
	
}
