package nio;

import java.nio.channels.SocketChannel;

public interface ChannelConnectedListener {
	public void ChannelConnected(SocketChannel channel);
}
