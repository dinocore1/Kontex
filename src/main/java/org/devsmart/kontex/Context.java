package org.devsmart.kontex;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.devsmart.kontex.bencode.BDecoder;
import org.devsmart.kontex.bencode.BEValue;


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

	public static final Logger sLogger = Logger.getLogger("Context");

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

					case Packet.TYPE_GETPEERS:
						if(packet.isAck()){
							decodeGetPeersPacket(packet);
						} else {
							sendGetPeersResponse(packet);
						}
						break;
					}



				}
			}

		});
	}

	private void decodeGetPeersPacket(Packet packet){
		try {
			BEValue value = BDecoder.bdecode(packet.getPayload());
			for(BEValue peervalue : value.getList()){
				Peer newPeer = new Peer();
				newPeer.decode(peervalue);
				mRouter.addPeer(newPeer);
			}
		} catch (IOException e) {
			sLogger.log(Level.SEVERE, "error decoding getpeers response packet", e);
		}
	}

	private void sendGetPeersResponse(Packet requestPacket) {
		try {
			InputStream input = requestPacket.getPayload();
			Id targetId = new Id();
			targetId.read(input);
			
			LinkedList<Peer> peerList = new LinkedList<Peer>();
			Iterator<Peer> it = mRouter.getBucket(targetId).iterator();
			int i = 0;
			while(it.hasNext() && i++<8){
				peerList.add(it.next());
			}
			
			Packet responsePacket = PacketFactory.createGetPeersResponsePacket(mId, requestPacket.mFrom, peerList);
			sendPacket(responsePacket, requestPacket.mFromSocketAddress);
			
		} catch (IOException e) {
			sLogger.log(Level.WARNING, "error decoding getpeers request packet", e);
		}
	}

	public void sendPacket(Packet p, InetSocketAddress address){
		mNetwork.sendPacket(p, address);
	}



}
