package directory;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class FileTransactionRecord implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final Date timestamp;
	public final UUID id;
	public final UUID file_id;
	public final TransactionType transactype;
	public FileTransactionRecord(TransactionType transt, DFile file) {
		timestamp = new Date();
		id = UUID.randomUUID();
		transactype = transt;
		file_id = file.id;
	}
}
