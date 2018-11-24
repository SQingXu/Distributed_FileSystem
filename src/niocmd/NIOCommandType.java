package niocmd;

public enum NIOCommandType {
	//DataNode/Client File Operations
	SEND_FILE_DATA,
	RECEIVE_FILE_DATA,
	REMOVE_FILE_DATA,
	
	//NameNode File Operations
	UPLOAD_FILE_NAME,
	DOWNLOAD_FILE_NAME,
	
	//NameNode Directory Operations
	CREATE_DIR,
	REMOVE_DIR_FILE,
	MOVE_DIR_FILE,
	RENAME_DIR_FILE,
	
	SET_CURRENT_DIRECTORY,
	PRINT_WORKING_DIRECTORY,
	LIST_WORKING_DIRECTORY,
	
	//NameNode Feedback
	RESULT_FEED,
	
	//Client/DataNode Feedback
	SEND_FILE_FEED,
	RECEIVE_DATA_FEED,
	REMOVE_FILE_FEED,
	NOTCONTAIN_FILE_FEED,
	
	//Client status request
	LOAD_BALANCE_STATUS
	
}
