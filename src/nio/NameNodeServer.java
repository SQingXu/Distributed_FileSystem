package nio;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import backup.SerializingBackup;
import directory.DirectoryController;
import loadbalance.LoadBalance;

public class NameNodeServer implements Runnable{
	public static NameNodeServer server = new NameNodeServer();
	
	//server components
	public ServerSocketChannel serverChannel;
	public Selector selector;
	public NameNodeServerReader reader;
	public ServerWriter writer;
	public SerializingBackup backuper;
	public LoadBalance loadBalanceStatus;
	
	//configuration
	public String backup_dir = "/Users/davidxu/Desktop/Java";
	public int replication_number = 2;
	public int changeThreshold = 1;
	public List<DataNodeAddress> dataAddresses;
	
	//helper threads
	public Thread read_thread;
	public Thread write_thread;
	public Thread select_thread;
	public Thread backup_thread; 
	
	//alive datanode to opened channels
	public Map<DataNodeAddress, SocketChannel> dataNodeChannels; 
	//alive channel
	public Map<InetSocketAddress, SocketChannel> addrChannels;
	
	protected NameNodeServer() {
		dataAddresses = new ArrayList<>();
		dataNodeChannels = new HashMap<>(); 
		addrChannels = new HashMap<>();
	}
	
	public void init(String host, int port) {
		try {
			//writer
			writer = new ServerWriter(1024);
			write_thread = new Thread(writer);
			write_thread.start();
			
			//reader
			reader = new NameNodeServerReader(1024, writer, server);
			read_thread = new Thread(reader);
			read_thread.start();
			
			//loadbalance
			loadBalanceStatus = new LoadBalance(server);
			
			//backup
			backuper = new SerializingBackup(backup_dir, changeThreshold, server);
			backup_thread = new Thread(backuper);
			backup_thread.start();
			
			//we can do backup before the server started if there is a backup file
			System.out.println(DirectoryController.instance.root_dir.name);
			if(!backuper.DeserializeFromFile()) {
				System.out.println("no back up found at directory " + backup_dir);
			}
			
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(host, port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			
			int ops = serverChannel.validOps();
			
			serverChannel.register(selector, ops, null);
			
		}catch(Exception e) {
			e.printStackTrace();
			System.err.println("server socket initilization error");
		}
		
	}
	
	public void syncSelect() {
		select_thread = new Thread(server);
		select_thread.start();
	}
	
	public DataNodeAddress containsNodeAddress(InetSocketAddress address) {
		for(DataNodeAddress addr:this.dataAddresses) {
			if(addr.getServerAddress().equals(address)) {
				return addr;
			}
			if(addr.getNameConnectedAddress().equals(address)) {
				return addr;
			}
		}
		return null;
	}
	

	@Override
	public void run() {
		while(true) {
			try {
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while(iterator.hasNext()) {
					SelectionKey key = iterator.next();
					if(!key.channel().isOpen()) {
						key.cancel();
						continue;
					}
					if(!key.isValid()) {
						System.err.println("invalid keys");
					}else if(key.isAcceptable()) {
						SocketChannel clientChannel = serverChannel.accept();
						clientChannel.configureBlocking(false);
						clientChannel.register(selector, SelectionKey.OP_READ);
						Socket info = clientChannel.socket();
						//add datanode channel for later uses
						DataNodeAddress dna = containsNodeAddress((InetSocketAddress)clientChannel.getRemoteAddress());
						if(dna != null) {
							dataNodeChannels.put(dna, clientChannel);
							System.out.println("datanode " + dna.getId() + " is connected via " + dna.getNameConnectedAddress());
							dna.printAddress();
						}
						addrChannels.put((InetSocketAddress)clientChannel.getRemoteAddress(), clientChannel);
						System.out.println("New Connected Channel: ");
						System.out.println(info.getRemoteSocketAddress().toString());
						
					}else if(key.isReadable()) {
						//first read a commandMsg object
						
						SocketChannel channel = (SocketChannel)key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						int result = channel.read(buffer);
						if(result < 0) {
							//the peer channel is closed
							System.out.println("channel: " + channel.getRemoteAddress() + " is closed remotely");
							DataNodeAddress dna = containsNodeAddress((InetSocketAddress)channel.getRemoteAddress());
							if(dna != null) {
								dataNodeChannels.remove(dna);
								System.out.println("the datanode channel is removed");
							}
							SocketChannel removed_channel = addrChannels.remove((InetSocketAddress)channel.getRemoteAddress());
							if(removed_channel == null) {
								System.err.println("no correct channel is found");
							}
							reader.channelClosed(channel);
							channel.close();
						}
						buffer.flip();
						//ByteBuffer ret = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());
						
						ByteBuffer mBuffer = ByteBuffer.wrap(buffer.array(),0,buffer.remaining());;
						byte[] anArrayCopy = Arrays.copyOfRange(mBuffer.array(), mBuffer.position(), mBuffer.limit());
						ByteBuffer dBuffer = ByteBuffer.wrap(anArrayCopy,0, mBuffer.remaining());
						
						ByteBufferWSource dBufferSource = new ByteBufferWSource(dBuffer,channel);
						reader.addBufferWSource(dBufferSource);
						//buffer.remaining() = length
						buffer.clear();
						
					}
					//separate out write operation of channel
//					SelectableChannel schannel = key.channel();
//					if(schannel.equals(serverChannel)) {
//						continue;
//					}
//					SocketChannel channel = (SocketChannel)schannel;
//					if(!key.isWritable()) {
//						System.out.println("namenode key is not writable at channel : " + channel.socket().getPort() + " at " + 
//						channel.socket().toString());
//						writer.channelStatusUpdate(false, channel);
//					}else {
//						System.out.println("namenode key is writable at channel : " + channel.socket().getPort() + " at " + 
//						channel.socket().toString());
//						writer.channelStatusUpdate(true, channel);
//					}
					
				}
				iterator.remove();
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}

