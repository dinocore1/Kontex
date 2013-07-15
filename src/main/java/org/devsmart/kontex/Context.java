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
		context.mRouter = new Router();
		context.mRouter.mContext = context;
		context.mId = id;
		
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
				
				Peer fromPeer = new Peer(packet.mFromSocketAddress, packet.mFromId);
				
				switch(packet.mType){
				case Ping:
					mRouter.addPeer(fromPeer);
					sendPong(fromPeer);
					break;
					
				case Pong:
					mRouter.addPeer(fromPeer);
					break;
				}
				
			}
			
		});
	}
	
	void sendPing(Peer peer) {
		Packet packet = Packet.createPing(mId);
		mNetwork.sendPacket(packet, peer.mAddress);
	}
	
	void sendPong(Peer peer) {
		Packet packet = Packet.createPong(mId);
		mNetwork.sendPacket(packet, peer.mAddress);
	}
	
}
