package nio;

import java.nio.ByteBuffer;

public class ByteBufferTranslator {
	public static ByteBuffer addHeader(String str) {
		byte[] bytes = str.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
}
