package nio;

import java.nio.ByteBuffer;

public class AcumulateBuffer {
	public ByteBuffer lengthBuffer;
	public ByteBuffer gatheredBuffer;
	public final int lenBuffer_len = 4;
	public boolean getLen;
	
	public AcumulateBuffer() {
		this.lengthBuffer = ByteBuffer.allocate(lenBuffer_len);
		this.getLen = false;
	}
	
	public boolean acumulate(ByteBuffer buffer) {
		if(!getLen) {
			if(buffer.remaining() >= lengthBuffer.remaining()) {
				int advanced = lengthBuffer.remaining();
				lengthBuffer.put(buffer.array(), buffer.position(), advanced);
				//update length buffer
				buffer.position(buffer.position()+advanced);
				lengthBuffer.flip();
				int buffer_length = lengthBuffer.getInt();
				lengthBuffer.clear();
				getLen = true;
				
				//allocate for that amount of buffer 
				gatheredBuffer = ByteBuffer.allocate(buffer_length);
			}else {
				lengthBuffer.put(buffer.array(), buffer.position(),buffer.remaining());
				buffer.position(buffer.position() + buffer.remaining());
			}
			return false;
		}else {
			boolean ret = false;
			int advanced = Math.min(gatheredBuffer.remaining(), buffer.remaining());
			gatheredBuffer.put(buffer.array(), buffer.position(), advanced);
			if(gatheredBuffer.remaining() == 0) {
				getLen = false;
				gatheredBuffer.flip();
				//processCmd(gatheredBuffer, buffer_source.channel);
				//gatheredBuffer.clear();
				ret = true;
			}
			buffer.position(buffer.position()+ advanced);
			return ret;
		}
	}
}
