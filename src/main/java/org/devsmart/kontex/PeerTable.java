package org.devsmart.kontex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;

public class PeerTable {
	
	public static final Logger sLogger = Logger.getLogger("PeerTable");

	private final int NUM_BITS = Id.NUM_BYTES*8;
	
	protected ArrayList< LinkedList<Peer> > mTable = new ArrayList< LinkedList<Peer> >(NUM_BITS);
	public final Id mId;
	
	public PeerTable(Id id) {
		mId = id;
		for(int i=0;i<NUM_BITS;i++){
			mTable.add(new LinkedList<Peer>());
		}
	}
	
	public void addPeer(Peer p) {
		LinkedList<Peer> bucket = getBucket(p.mId);
		bucket.add(p);
	}

	public void removePeer(Peer peer) {
		LinkedList<Peer> bucket = getBucket(peer.mId);
		bucket.remove(peer);
	}
	
	public static Peer findPeer(Peer p, LinkedList<Peer> bucket){
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
			
			int result = (id.mIdBytes[bytenum] & bitnum) ^ (mId.mIdBytes[bytenum] & bitnum);
			if(result == 0){
				bucket = mTable.get(i);
				break;
			}
		}
		return bucket;
	}


	
	
}
