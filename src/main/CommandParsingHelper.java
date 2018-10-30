package main;

import java.util.ArrayList;
import java.util.List;


import niocmd.NIOCommand;
import niocmd.NIOCommandType;



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
	public static NIOCommand parseCmdString(String cmdStr) throws InvalidCommandException{
		List<String> argList = separateString(cmdStr);
		NIOCommandType cmdType = parseFirstPart(argList.get(0));
		String[] args;
		if((cmdType.equals(NIOCommandType.PRINT_WORKING_DIRECTORY) || cmdType.equals(NIOCommandType.LIST_WORKING_DIRECTORY) &&
				argList.size() == 1)) {
			args = new String[1];
			args[0] = ".";
			return new NIOCommand(cmdType,args) ;
		}else if((cmdType.equals(NIOCommandType.CREATE_DIR) || cmdType.equals(NIOCommandType.LIST_WORKING_DIRECTORY) 
				|| cmdType.equals(NIOCommandType.REMOVE_DIR_FILE) || cmdType.equals(NIOCommandType.DOWNLOAD_FILE_NAME) 
				|| cmdType.equals(NIOCommandType.UPLOAD_FILE_NAME) || cmdType.equals(NIOCommandType.SET_CURRENT_DIRECTORY))
				&& argList.size() == 2) {
			args = new String[1];
			args[0] = argList.get(1);
			return new NIOCommand(cmdType, args);
		}else if((cmdType.equals(NIOCommandType.RENAME_DIR_FILE) || cmdType.equals(NIOCommandType.MOVE_DIR_FILE)
				|| cmdType.equals(NIOCommandType.DOWNLOAD_FILE_NAME) || cmdType.equals(NIOCommandType.UPLOAD_FILE_NAME)) 
				&& argList.size() == 3) {
			args = new String[2];
			args[0] = argList.get(1);
			args[1] = argList.get(2);
			return new NIOCommand(cmdType, args);
		}else {
			System.out.println("agr1: " + argList.get(1) + " arg2: " + argList.get(1) );
			throw new InvalidCommandException("incorrect number of arguments with " + argList.size() + " arguments");
		}
		
	}
	
	public static List<String> separateString(String cmdStr) {
		List<String> ret = new ArrayList<String>();
		char pre_ch = 'n';
		char pre_pre_ch = 'n';
		int start = 0;
		int end = 0;
		for(int i = 0; i < cmdStr.length(); i++) {
			char ch = cmdStr.charAt(i);
			if(ch == ' ' && pre_ch != '\\') {
				end = i;
				if(start < end && (pre_ch != ' ' || (pre_ch == ' ' && pre_pre_ch == '\\'))) {
					//the sub-string has length and the there are consecutive space
					ret.add(cmdStr.substring(start, end));
				}
				start = end+1;
			}
			pre_pre_ch = pre_ch;
			pre_ch = ch;
			
		}
		if(start <= cmdStr.length()-1) {
			ret.add(cmdStr.substring(start,cmdStr.length()));
		}
		removeForwardSlash(ret);
		return ret;
	}
	
	private static void removeForwardSlash(List<String> sub_strs) {
		for(int j = 0; j < sub_strs.size(); j++) {
			String sub = sub_strs.get(j);
			char pre_ch = 'l';
			int start = 0;
			int end = 0;
			boolean changed = false;
			String new_sub = "";
			for(int i = 0; i < sub.length(); i++) {
				char ch = sub.charAt(i);
				if(ch == ' ' && pre_ch == '\\') {
					//recognize a space
					end = i-1;
					if(start < end) {
						new_sub += sub.substring(start, end)+' ';
					}
					start = i+1;
					changed = true;
				}
				pre_ch = ch;
			}
			if(changed) {
				if(start < sub.length()) {
					new_sub += sub.substring(start,sub.length());
				}
				sub_strs.set(j, new_sub);
			}
		}
	}
	
	
	private static NIOCommandType parseFirstPart(String first_part) throws InvalidCommandException{
		String sub_head = first_part;
		switch(sub_head) {
		case "cd":
			return NIOCommandType.SET_CURRENT_DIRECTORY;
		case "pwd":
			return NIOCommandType.PRINT_WORKING_DIRECTORY;
		case "ls":
			return NIOCommandType.LIST_WORKING_DIRECTORY;
		case "mv":
			return NIOCommandType.MOVE_DIR_FILE;
		case "mkdir":
			return NIOCommandType.CREATE_DIR;
		case "rn":
			return NIOCommandType.RENAME_DIR_FILE;
		case "rm":
			return NIOCommandType.REMOVE_DIR_FILE;
		case "upload":
			return NIOCommandType.UPLOAD_FILE_NAME;
		case "download":
			return NIOCommandType.DOWNLOAD_FILE_NAME;
		default:
			throw new InvalidCommandException("command not recognized");
		}
	}
	
	
	
	
}
