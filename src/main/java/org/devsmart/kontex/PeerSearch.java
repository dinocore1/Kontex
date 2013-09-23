package org.devsmart.kontex;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.devsmart.kontex.ConnectionManager.ConnectionCallback;

public class PeerSearch implements PeerListener {
	public static final Logger sLogger = Logger.getLogger("PeerSearchObj");
	public static final int MAX_PARALLEL_SEARCH = 3;
	public static final int DEFAULT_WAIT = 10000;

	public static enum State {
		Idle,
		Searching,
		Connecting
	}

	private State mState = State.Idle;
	private Context mContext;
	public final Id mTarget;
	private TreeSet<Peer> mConnectQueue;
	private int mCurrentSearches = 0;
	private HashSet<ConnectionCallback> mCallbacks = new HashSet<ConnectionCallback>();


	public PeerSearch(Id target, Context context) {
		mTarget = target;
		mContext = context;
		mConnectQueue = new TreeSet<Peer>(new Utils.PeerDistanceComparator(target));
	}

	public void start() {
		if(mState != State.Idle){
			sLogger.warning("cannot start search when not idle");
			return;
		}
		sLogger.fine("Starting search for target: " + mTarget);
		mContext.addPeerListener(this);
		startSearch();

	}

	public void stop() {
		mContext.removePeerListener(this);
	}

	@Override
	protected void finalize(){
		stop();
	}

	public void restartSearch() {
		startSearch();
	}

	private synchronized void startSearch() {
		mContext.mMainThread.execute(new Runnable(){
			@Override
			public void run() {
				synchronized(PeerSearch.this){
					for(List<Peer> bucket : mContext.mPeerTable.mTable){
						for(Peer p : bucket){
							mConnectQueue.add(p);
						}
					}
					mState = State.Searching;
					searchPeer();
				}
			}
		});
	}

	@Override
	public void onNewPeer(Peer peer) {

		if(mTarget.equals(peer.mId)){
			sLogger.fine("Found peer: " + peer);
			synchronized(PeerSearch.this){
				mState = State.Connecting;
			}

			try {
				mContext.sendPacket(PacketFactory.createConnectPacket(mContext.mId, mTarget, null), peer.mAddress);

				for(Peer viaPeer : peer.mVia){
					mContext.sendPacket(PacketFactory.createConnectPacket(mContext.mId, mTarget, null), viaPeer.mAddress);
				}
			} catch(IOException e) {
				sLogger.log(Level.SEVERE, "", e);
			}

		} else {
			synchronized(PeerSearch.this){
				mConnectQueue.add(peer);
			}
			if(mState == State.Searching) {
				searchPeer();
			}
		}


	}

	private synchronized void searchPeer() {
		if(mState == State.Searching && mCurrentSearches < MAX_PARALLEL_SEARCH){
			final Peer searchPeer = mConnectQueue.pollFirst();
			if(searchPeer == null){
				endWithTimeout();
				return;
			}
			mCurrentSearches++;
			Utils.sIOThreads.execute(new Runnable(){

				@Override
				public void run() {
					try {
						Packet p = PacketFactory.createGetPeersRequestPacket(mContext.mId, searchPeer.mId, mTarget);
						mContext.sendPacket(p, searchPeer.mAddress);
						mContext.mMainThread.schedule(mTimeoutRunnable, DEFAULT_WAIT, TimeUnit.MILLISECONDS);
					} catch(IOException e){
						sLogger.log(Level.SEVERE, "", e);
					}
				}

			});
		}

	}
	
	private Runnable mTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
			synchronized(PeerSearch.this){
				mCurrentSearches--;
				searchPeer();
			}
			
		}
		
	};

	@Override
	public void onDeadPeer(Peer peer) {
		synchronized(this){
			mConnectQueue.remove(peer);
		}
	}

	@Override
	public void onDieingPeer(Peer peer) {}

	public void addCallback(ConnectionCallback cb) {
		mCallbacks.add(cb);
		
	}
	
	private void endWithTimeout(){
		sLogger.fine("No more peers to search");
		Utils.sIOThreads.execute(new Runnable(){

			@Override
			public void run() {
				synchronized(PeerSearch.this){
					mState = State.Idle;
					mContext.mConnectionManager.mSearches.remove(mTarget);
					for(ConnectionCallback cb : mCallbacks){
						cb.onTimeout(mTarget);
					}
				}
				
			}
			
		});
	}

	public void onRecieveDatagram(Packet packet) {
		// TODO Auto-generated method stub
		
	}


}
