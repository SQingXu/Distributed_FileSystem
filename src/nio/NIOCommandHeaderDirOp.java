package nio;

public class NIOCommandHeaderDirOp extends NIOCommandHeader{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String dir_str;
	public String destination = "";
	public NIOCommandHeaderDirOp(NIOCommandType type, String dir_str) {
		super();
		this.type = type;
		this.dir_str = dir_str;
	}
	
	public NIOCommandHeaderDirOp(NIOCommandType type, String dir_str, String destination) {
		super();
		this.type = type;
		this.dir_str = dir_str;
		this.destination = destination;
	}
	

}
