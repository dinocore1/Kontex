package org.devsmart.kontext;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Context {
	
	public static Context createContext() {
		Context context = new Context();
		context.mRouter = new Router();
		context.mRouter.mContext = context;
		context.mId = new Id();
		
		return context;
	}
	
	public ScheduledExecutorService mMainThread = Executors.newSingleThreadScheduledExecutor();
	public InetSocketAddress mSocket;
	public Id mId;
	public Router mRouter;
	
	
	
	public void onReciveDatagram(final Packet packet) {
		mMainThread.execute(new Runnable(){

			public void run() {
				switch(packet.mType){
				case Ping:
					mRouter.addPeer(new Peer(packet.mFromSocketAddress, packet.mFromId));
					break;
				}
				
			}
			
		});
	}
	
}
