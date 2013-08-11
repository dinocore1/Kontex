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
	 * 40 |   PACKET TYPE  |A 1 2 3 4 5 6 7 |         CHECKSUM                |
	 *    |                |C
	 *    |                |K
	 */   

	public static final int TYPE_KEEPALIVE = 0x0;
	public static final int TYPE_CONNECTION = 0x1;
	public static final int TYPE_GETPEERS = 0x2;

	protected byte[] mData;
	protected InetSocketAddress mFromSocketAddress;
	protected Id mFrom;
	protected Id mTo;

	public static Packet parseDatagram(DatagramPacket datagram) {
		int length = datagram.getLength();
		if(length < 44){
			return null;
		}

		byte[] data = datagram.getData();

		//check the checksum
		final int packetChecksum = (((int)data[42] << 8) | ((int)data[43] & 0xff)) & 0xffff;
		int calcChecksum = CRC16.crc(0, data, 0, 42);
		calcChecksum = CRC16.crc(calcChecksum, data, 44, datagram.getLength()-44);
		if(packetChecksum != calcChecksum){
			return null;
		}

		Packet retval = new Packet();
		retval.mFrom = new Id(data, 0);
		retval.mTo = new Id(data, 20);
		retval.mFromSocketAddress = new InetSocketAddress(datagram.getAddress(), datagram.getPort());
		retval.mData = new byte[datagram.getLength()];
		System.arraycopy(datagram.getData(), datagram.getOffset(), retval.mData, 0, datagram.getLength());

		return retval;
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

	public void writeChecksum() {
		int checksum = CRC16.crc(0, mData, 0, 42);
		checksum = CRC16.crc(checksum, mData, 44, mData.length-44);
		mData[42] = (byte) ((checksum & 0xff00) >> 8);
		mData[43] = (byte) (checksum & 0xff);
	}

	public void setType(int type) {
		mData[40] = (byte) type;
	}
	
	public ByteArrayInputStream getPayload() {
		return new ByteArrayInputStream(mData, 44, mData.length - 44);
	}
	
	private Peer mFromPeer;
	public Peer getFromPeer() {
		if(mFromPeer == null){
			mFromPeer = new Peer(mFromSocketAddress, mFrom);
		}
		return mFromPeer;
	}
	
}
