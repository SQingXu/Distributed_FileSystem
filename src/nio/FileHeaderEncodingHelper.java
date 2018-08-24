package nio;

import java.nio.ByteBuffer;

public class FileHeaderEncodingHelper {
	static private String digitsString(int digits) {
		if(digits >= 10) {
			//assume no digits can exceed 90
			return String.valueOf(digits);
		}else {
			return "0" + String.valueOf(digits);
		}
	}
	static int getDigits(int length) {
		int divisor = 1;
		int ret = 0;
		while(length/divisor > 0) {
			divisor *= 10;
			ret += 1;
		}
		return ret;
	}
	
	static public ByteBufferHeaderInfo getHeaderLength(ByteBuffer buffer) {
		
		String digits_str = new String(buffer.array(),0,2);
		int digits;
		if(Integer.parseInt(digits_str.substring(0, 1)) == 0) {
			digits = Integer.parseInt(digits_str.substring(1));
		}else {
			digits = Integer.parseInt(digits_str);
		}
		String length_str = new String(buffer.array(),2,digits);
		ByteBufferHeaderInfo ret = new ByteBufferHeaderInfo(2+digits, Integer.parseInt(length_str));
		return ret;
	}
	
	static public String addLengthHeader(String data) {
		int length = data.length();
		int digits = getDigits(length);
		String ret = digitsString(digits) + String.valueOf(length) + data;
		return ret;
	}
	
	
}
