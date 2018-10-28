package niocmd;

import java.io.IOException;

import nio.NIOSerializer;

public class NIOCommandFactory {
	public static NIOCommand commandSendFile(SendFileObject sfo) {
		String[] args = new String[1];
		try {
			args[0] = NIOSerializer.toString(sfo);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		NIOCommand cmd = new NIOCommand(NIOCommandType.SEND_FILE_DATA, args);
		return cmd;
	}
	
	public static SendFileObject fromCmdSendFile(NIOCommand cmd) {
		if(cmd.type != NIOCommandType.SEND_FILE_DATA) {
			return null;
		}
		SendFileObject sfo = null;
		try {
			sfo = (SendFileObject)NIOSerializer.FromString(cmd.args[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		return sfo;
	}
	
	public static NIOCommand commandReceiveFile(ReceiveFileObject rfo) {
		String[] args = new String[1];
		try {
			args[0] = NIOSerializer.toString(rfo);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		NIOCommand cmd = new NIOCommand(NIOCommandType.RECEIVE_FILE_DATA, args);
		return cmd;
	}
	
	public static ReceiveFileObject fromCmdReceiveFile(NIOCommand cmd) {
		if(cmd.type != NIOCommandType.RECEIVE_FILE_DATA) {
			return null;
		}
		ReceiveFileObject rfo = null;
		try {
			rfo = (ReceiveFileObject)NIOSerializer.FromString(cmd.args[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		return rfo;
	}
	
}
