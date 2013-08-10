package org.devsmart.kontex;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Context {
	
	

	public static Context createContext() throws SocketException {
		return createContext(9000, new Id());
	}
	
	public static Context createContext(int port, Id id) throws SocketException {
		Context context = new Context();
		initialize(context, port, id);
		return context;
	}
	
	public static void initialize(Context context, int port, Id id) throws SocketException {
		context.mId = id;
		
		context.mRouter = new Router();
		context.mRouter.mContext = context;
		
		context.mNetwork = new NetworkListener();
		context.mNetwork.mContext = context;
		
		context.mNetwork.start(new InetSocketAddress(port));
	}
	
	public ScheduledExecutorService mMainThread = Executors.newSingleThreadScheduledExecutor();
	public Id mId;
	public Router mRouter;
	public NetworkListener mNetwork;
	
	
	public void onReciveDatagram(final Packet packet) {
		mMainThread.execute(new Runnable(){

			public void run() {
				
				if(packet.mTo.equals(mId)){
					//the incoming packet is addressed to this node
					
					Peer peer = new Peer(packet.mFromSocketAddress, packet.mFrom);
					mRouter.addPeer(peer);
					
					switch(packet.getPacketType()){
					case Packet.TYPE_KEEPALIVE:
						if(!packet.isAck()){
							Packet pong = PacketFactory.createPongPacket(mId, packet.mFrom);
							sendPacket(pong, packet.mFromSocketAddress);
						}
						break;
					}
					
					
					
				}
				
			}
			
		});
	}
	
	public void sendPacket(Packet p, InetSocketAddress address){
		mNetwork.sendPacket(p, address);
	}
	
	
	
}
