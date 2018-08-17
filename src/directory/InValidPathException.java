package directory;

public class InValidPathException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InValidPathException(String message) {
		super(message);
	}
	
	public InValidPathException() {
		super();
	}
	
	public InValidPathException(Throwable cause) {
        super(cause);
    }

    public InValidPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
