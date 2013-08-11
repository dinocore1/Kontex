package org.devsmart.kontex;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.devsmart.kontex.Utils.PeerDistanceComparator;

public class ConnectionManager {

	Context mContext;
	HashMap<Id, Connection> mConnections = new HashMap<Id, Connection>();
	
	public Connection connect(Id target) {
		Connection retval;
		
		//first check if this connection already exists
		retval = mConnections.get(target);
		if(retval != null){
			return retval;
		}
		
		PeerSearchObj connectObj = new PeerSearchObj(target);
		mContext.addPeerListener(connectObj);
		
		
		return retval;
	}
	
	private class PeerSearchObj implements PeerListener {

		public Id mTarget;
		public PeerTable mPeerTable;
		private PeerDistanceComparator mComparator;

		public PeerSearchObj(Id target) {
			mTarget = target;
			mComparator = new Utils.PeerDistanceComparator(target);
			mPeerTable = new PeerTable(target);
			for(List<Peer> bucket : mContext.mPeerTable.mTable){
				for(Peer p : bucket){
					onNewPeer(p);
				}
			}
		}

		@Override
		public void onNewPeer(Peer peer) {
			LinkedList<Peer> bucket = mPeerTable.getBucket(peer.mId);
			if(!bucket.contains(peer)){
				bucket.add(peer);
				Collections.sort(bucket, mComparator);
				if(bucket.size() > Context.MAX_BUCKET_SIZE){
					bucket.removeLast();
				}
			}
		}

		@Override
		public void onDeadPeer(Peer peer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void onDieingPeer(Peer peer) {
			throw new UnsupportedOperationException();
		}
		
		
	}

}
