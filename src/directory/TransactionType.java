package directory;

public enum TransactionType {
	
	CREATEFILE(0),
	DELETEFILE(1),
	MODIFYFILE(2);
	
	private int value;
	public int valueOf() {
		return value;
	}
	private TransactionType(int v) {
		this.value = v;
	}
}
