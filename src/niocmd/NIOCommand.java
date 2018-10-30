package niocmd;

import java.io.Serializable;

public class NIOCommand implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NIOCommandType type;
	public String[] args;
	public NIOCommand(NIOCommandType t, String[] args) {
		this.type = t;
		//copy the args
		this.args = new String[args.length];
		for(int i = 0; i < args.length; i++) {
			this.args[i] = args[i];
		}
	}
	
}