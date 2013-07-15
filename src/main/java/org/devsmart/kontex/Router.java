package org.devsmart.kontex;

import java.util.ArrayList;
import java.util.LinkedList;

public class Router {

	private final int NUM_BITS = Id.NUM_BYTES*8;
	private final int MAX_BUCKET_SIZE = 10;
	
	protected ArrayList< LinkedList<Peer> > mTable = new ArrayList< LinkedList<Peer> >(NUM_BITS);
	
	protected Context mContext;
	
	public Router() {
		for(int i=0;i<NUM_BITS;i++){
			mTable.add(new LinkedList<Peer>());
		}
	}
	
	public void addPeer(Peer peer) {
		LinkedList<Peer> bucket = getBucket(peer.mId);
		
		Peer existingPeer = findPeer(peer, bucket);
		if(existingPeer != null) {
			existingPeer.mLastSeen = Utils.getUptime();
		}
		
		if(existingPeer == null && bucket.size() < MAX_BUCKET_SIZE) {
			peer.mLastSeen = Utils.getUptime();
			bucket.addLast(peer);
			peer.startMaintance(mContext);
		}
	}
	
	public void removePeer(Peer peer) {
		LinkedList<Peer> bucket = getBucket(peer.mId);
		bucket.remove(peer);
		peer.stopMaintance();
		
	}
	
	private Peer findPeer(Peer p, LinkedList<Peer> bucket){
		for(Peer a : bucket){
			if(a.equals(p)){
				return a;
			}
		}
		return null;
	}
	
	public LinkedList<Peer> getBucket(Id id) {
		LinkedList<Peer> bucket = null;
		for(int i=0;i<NUM_BITS;i++){
			int bytenum = i/8;
			int bitnum = 1 << (7 - i%8);
			
			int result = (id.mIdBytes[bytenum] & bitnum) ^ (mContext.mId.mIdBytes[bytenum] & bitnum);
			if(result > 0){
				bucket = mTable.get(i);
				break;
			}
		}
		return bucket;
	}

	
	
}
