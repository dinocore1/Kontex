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
		LinkedList<Peer> bucket = getBucket(peer);
		
		
		if(!bucket.contains(peer) && bucket.size() < MAX_BUCKET_SIZE) {
			bucket.addLast(peer);
			peer.startMaintance(mContext);
		}
		
	}
	
	public LinkedList<Peer> getBucket(Peer peer) {
		LinkedList<Peer> bucket = null;
		for(int i=0;i<NUM_BITS;i++){
			int bytenum = i/8;
			int bitnum = 1 << (7 - i%8);
			
			int result = (peer.mId.mIdBytes[bytenum] & bitnum) ^ (mContext.mId.mIdBytes[bytenum] & bitnum);
			if(result > 0){
				bucket = mTable.get(i);
				break;
			}
		}
		return bucket;
	}
	
}
