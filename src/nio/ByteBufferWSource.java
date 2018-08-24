package nio;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ByteBufferWSource {
	public ByteBuffer buffer;
	public SocketChannel channel;
	public ByteBufferWSource(ByteBuffer buffer, SocketChannel channel) {
		this.buffer = buffer;
		this.channel = channel;
	}
}
