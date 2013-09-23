package org.devsmart.kontex;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Packet {

	/**
	 *    +----------------+----------------+----------------+----------------+
	 *    |        0       |        1       |        2       |         3      |
	 *    +----------------+----------------+----------------+----------------+
	 *  0 |                        20 byte Source ...                         |
	 *    |    .....                                                          |
	 *    +-------------------------------------------------------------------+
	 *    |                        20 byte Destination      ...               |
	 *    |   .....                                                           |
	 *    +----------------+----------------+---------------------------------+
	 * 40 |   PACKET TYPE  |A 1 2 3 4 5 6 7 |         PORT NUM                |
	 *    |                |C
	 *    |                |K
	 */   

	public static final int TYPE_KEEPALIVE = 0x0;
	public static final int TYPE_CONNECTION = 0x1;
	public static final int TYPE_GETPEERS = 0x2;

	protected byte[] mData;
	protected InetSocketAddress mFromSocketAddress;

	public static Packet parseDatagram(DatagramPacket datagram) {
		int length = datagram.getLength();
		if(length < 44){
			return null;
		}

		Packet retval = new Packet();
		retval.mFromSocketAddress = new InetSocketAddress(datagram.getAddress(), datagram.getPort());
		retval.mData = new byte[datagram.getLength()];
		System.arraycopy(datagram.getData(), datagram.getOffset(), retval.mData, 0, datagram.getLength());

		return retval;
	}
	
	private Id mFrom;
	public Id getFrom() {
		if(mFrom == null){
			mFrom = new Id(mData, 0);
		}
		return mFrom;
	}
	
	private Id mTo;
	public Id getTo() {
		if(mTo == null){
			mTo = new Id(mData, 20);
		}
		return mTo;
	}
	
	public int getPacketType() {
		int retval = mData[40];
		return retval;
	}
	
	public boolean isAck() {
		int d = mData[41];
		return (d & 0x8) > 0;
	}

	public void setAck() {
		mData[41] = (byte) (mData[41] | 0x8);
	}

	public void setType(int type) {
		mData[40] = (byte) type;
	}
	
	public void setPort(int port) {
		mData[42] = (byte) ((port & 0xFF00) >> 8);
		mData[43] = (byte) (port & 0xFF);
	}
	
	public int getPort() {
		int port = 0;
		port = mData[42] << 8;
		port |= (mData[43] & 0xFF);
		port &= 0xFFFF;
		return port;
	}
	
	public ByteArrayInputStream getPayload() {
		return new ByteArrayInputStream(mData, 44, mData.length - 44);
	}
	
	private Peer mFromPeer;
	public Peer getFromPeer() {
		if(mFromPeer == null){
			mFromPeer = new Peer(mFromSocketAddress, getFrom());
		}
		return mFromPeer;
	}
	
}
