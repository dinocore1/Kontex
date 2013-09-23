package org.devsmart.kontex;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class Connection {
	
	public static enum State {
		Closed,
		HalfOpen,
		Open
	}

	private final TreeSet<Peer> mPeers;
	State mState = State.Closed;

	public Connection(Collection<Peer> peers) {
		if(peers.size() != 2){
			throw new RuntimeException("a connection must have exactly 2 peers");
		}
		mPeers = new TreeSet<Peer>(peers);
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
		int retval = 0;
		Iterator<Peer> it = mPeers.iterator();
		retval = it.next().hashCode() ^ it.next().hashCode();
		return retval;
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

	public void onRecieveDatagram(Packet packet) {
		// TODO Auto-generated method stub
		
	}

}
