package org.devsmart.kontex;

import java.util.HashMap;

public class ConnectionManager {
	
	public interface ConnectionCallback {
		void onConnect(Id id, Connection connection);
		void onTimeout(Id id);
	}

	Context mContext;
	HashMap<Id, Connection> mConnections = new HashMap<Id, Connection>();
	HashMap<Id, PeerSearch> mSearches = new HashMap<Id, PeerSearch>();
	
	public void connect(Id target, ConnectionCallback cb) {
	
		//first check if this connection already exists
		Connection connection = mConnections.get(target);
		if(connection != null){
			cb.onConnect(target, connection);
			return;
		}
		
		PeerSearch search = mSearches.get(target);
		if(!mSearches.containsKey(target)){
			search = new PeerSearch(target, mContext);
			mSearches.put(target, search);
			mContext.addPeerListener(search);
			search.start();
		}
		
		search.addCallback(cb);
		
	}

	public void onReciveDatagram(Packet packet) {
		Id from = packet.getFrom();
		Connection connection = mConnections.get(from);
		if(connection != null){
			connection.onRecieveDatagram(packet);
		} else {
			PeerSearch search = mSearches.get(from);
			if(search != null){
				search.onRecieveDatagram(packet);
			}
		}
		
	}


}
