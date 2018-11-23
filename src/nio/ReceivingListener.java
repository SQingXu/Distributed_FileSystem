package nio;

import java.util.UUID;

public interface ReceivingListener {
	public void notifyReceived(UUID id);
}
