package main;

public class InvalidCommandException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCommandException() {
		super();
	}
	
	public InvalidCommandException(String message) {
		super(message);
	}
	
	public InvalidCommandException(Throwable cause) {
		super(cause);
	}
	
	public InvalidCommandException(String message, Throwable cause) {
		super(message, cause);
	}
}
