package org.devsmart.kontex;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashSet;
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

		context.mPeerTable = new PeerTable(id);

		context.mNetwork = new NetworkListener();
		context.mNetwork.mContext = context;
		context.mNetwork.start(new InetSocketAddress(port));

		context.mConnectionManager = new ConnectionManager();
		context.mConnectionManager.mContext = context;
		
		context.mPeerListeners.add(context.new PeerMaintanceListener());
	}

	public static final Logger sLogger = Logger.getLogger("Context");
	final static int MAX_BUCKET_SIZE = 10;

	public ScheduledExecutorService mMainThread = Executors.newSingleThreadScheduledExecutor();
	public Id mId;
	public PeerTable mPeerTable;
	public NetworkListener mNetwork;
	public ConnectionManager mConnectionManager;
	private HashSet<PeerListener> mPeerListeners = new HashSet<PeerListener>();


	public void onReciveDatagram(final Packet packet) {
		mMainThread.execute(new Runnable(){

			public void run() {

				if(packet.mTo.equals(mId)){
					//the incoming packet is addressed to this node
					
					Peer peer = packet.getFromPeer();
					Peer existingPeer = PeerTable.findPeer(peer, mPeerTable.getBucket(peer.mId));
					if(existingPeer != null){
						existingPeer.mLastSeen = Utils.getUptime();
					} else {
						peer.mLastSeen = Utils.getUptime();
						broadcastNewPeerEvent(peer);
					}

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
				newPeer.addVia(packet.getFromPeer());
				broadcastNewPeerEvent(newPeer);
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
			Iterator<Peer> it = mPeerTable.getBucket(targetId).iterator();
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

	protected void addPeerListener(PeerListener listener){
		synchronized(mPeerListeners){
			mPeerListeners.add(listener);
		}
	}

	protected void removePeerListener(PeerListener listener){
		synchronized(mPeerListeners){
			mPeerListeners.remove(listener);
		}
	}

	protected void broadcastNewPeerEvent(final Peer peer) {
		mMainThread.execute(new Runnable(){

			@Override
			public void run() {
				synchronized(mPeerListeners){
					for(PeerListener pl : mPeerListeners){
						pl.onNewPeer(peer);
					}
				}
			}

		});
	}

	protected void broadcastDeadPeerEvent(final Peer peer) {
		mMainThread.execute(new Runnable(){

			@Override
			public void run() {
				synchronized(mPeerListeners){
					for(PeerListener pl : mPeerListeners){
						pl.onDeadPeer(peer);
					}
				}
			}

		});

	}

	public void broadcastDieingPeerEvent(final Peer peer) {
		mMainThread.execute(new Runnable(){

			@Override
			public void run() {
				synchronized(mPeerListeners){
					for(PeerListener pl : mPeerListeners){
						pl.onDieingPeer(peer);
					}
				}
			}

		});

	}
	
	private class PeerMaintanceListener implements PeerListener {

		@Override
		public void onNewPeer(Peer peer) {
			LinkedList<Peer> bucket = mPeerTable.getBucket(peer.mId);
			if(bucket.size() < MAX_BUCKET_SIZE){
				bucket.addLast(peer);
				peer.startMaintance(Context.this);
			}
			
		}

		@Override
		public void onDeadPeer(Peer peer) {
			Context.this.mPeerTable.removePeer(peer);
			peer.stopMaintance();
			
		}

		@Override
		public void onDieingPeer(Peer peer) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
