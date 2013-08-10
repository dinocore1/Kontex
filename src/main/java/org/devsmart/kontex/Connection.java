package org.devsmart.kontex;

import java.util.Iterator;
import java.util.SortedSet;

public class Connection {

	final SortedSet<Peer> mPeers;

	public Connection(SortedSet<Peer> peers) {
		if(peers.size() != 2){
			throw new RuntimeException("a connection must have exactly 2 peers");
		}
		mPeers = peers;
	}

	@Override
	public String toString() {
		StringBuilder retval = new StringBuilder();
		Iterator<Peer> it = mPeers.iterator();

		retval.append(it.next());
		retval.append(" <==> ");
		retval.append(it.next());

		return retval.toString();
	}
	
	@Override
	public int hashCode(){
		return mPeers.first().hashCode();
	}
	
	@Override
	public boolean equals(Object other){
		boolean retval = false;
		if(other instanceof Connection){
			Connection otherConnection = (Connection)other;
			Iterator<Peer> it = mPeers.iterator();
			retval = otherConnection.mPeers.contains(it.next()) && 
					otherConnection.mPeers.contains(it.next());
		}
		return retval;
	}

}
