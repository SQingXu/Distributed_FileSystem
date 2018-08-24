package nio;

public class NIOCommandHeaderDirOp extends NIOCommandHeader{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String dir_str;
	public String destination = "";
	public NIOCommandHeaderDirOp(String dir_str) {
		super();
		this.dir_str = dir_str;
	}
	
	public NIOCommandHeaderDirOp(String dir_str, String destination) {
		super();
		this.dir_str = dir_str;
		this.destination = destination;
	}
	

}
