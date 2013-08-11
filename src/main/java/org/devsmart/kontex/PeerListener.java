package org.devsmart.kontex;

public interface PeerListener {

	void onNewPeer(Peer peer);
	void onDeadPeer(Peer peer);
	void onDieingPeer(Peer peer);

}
