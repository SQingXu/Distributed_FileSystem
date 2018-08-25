package main;

import nio.NIOCommandHeader;
import nio.NIOCommandHeaderDirOp;
import nio.NIOCommandType;

enum CmdType{
	PWD, CD, LS, MV, RN, RM, MKDIR, UPLOAD, DOWNLOAD
}

public class CommandParsingHelper {
	/* 
	 * pwd: print current working directory
	 * cd: set current directory to the path string
	 * ls: list the sub-directories and sub-files in current directories
	 * mv: move file/directory to destination
	 * rn: rename directories or files to the name
	 * mkdir: create new directory
	 * rm: remove directory or files
	 * upload: upload files from local
	 * download: download files on the server 
	 * */ 
	public static NIOCommandHeader parseCmdString(String cmdStr) throws InvalidCommandException{
		int start = 0;
		int end = recognizeDivider(cmdStr, start);
		CmdType cmd = parseFirstPart(cmdStr, end);
		if(cmd.equals(CmdType.PWD)) {
			return new NIOCommandHeaderDirOp(NIOCommandType.PRINT_WORKING_DIRECTORY,"");
		}
		//ls can have either 1 or 0 path argument
		if(end == cmdStr.length() && cmd.equals(CmdType.LS)) {
			return new NIOCommandHeaderDirOp(NIOCommandType.LIST_WORKING_DIRECTORY, ".");
		}else if(end == cmdStr.length()) {
			throw new InvalidCommandException("command missing argument");
		}
		
		start = end+1;
		
		
		return null;
	}
	
	private static int recognizeDivider(String cmdStr, int start) {
		char pre_ch = 'n';
		for(int i = start; i < cmdStr.length(); i++) {
			char ch = cmdStr.charAt(i);
			if(ch == ' ' && pre_ch != '\\') {
				return i;
			}
			pre_ch = ch;
		}
		return cmdStr.length();
	}
	
	private static CmdType parseFirstPart(String cmdStr, int end) throws InvalidCommandException{
		String sub_head = cmdStr.substring(0, end);
		switch(sub_head) {
		case "cd":
			return CmdType.CD;
		case "pwd":
			return CmdType.PWD;
		case "ls":
			return CmdType.LS;
		case "mv":
			return CmdType.MV;
		case "mkdir":
			return CmdType.MKDIR;
		case "rn":
			return CmdType.RN;
		case "rm":
			return CmdType.RM;
		case "upload":
			return CmdType.UPLOAD;
		case "download":
			return CmdType.DOWNLOAD;
		default:
			throw new InvalidCommandException("command not recognized");
		}
	}
	
	
	
	
}
