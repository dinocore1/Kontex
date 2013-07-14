package org.devsmart.kontex;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Packet {

	public static enum Type {
		Ping,
		GetPeers,
		RouteTo
	}
	
	public Type mType;
	protected InetSocketAddress mFromSocketAddress;
	protected byte[] mData;
	protected Id mFromId;
	
	public static Packet parseDatagram(DatagramPacket datagram) {
		int length = datagram.getLength();
		if(length > 4){
			byte[] data = datagram.getData();
			
			//check magic packet header
			if((data[0] & 0xF0) != 0xA0){
				return null;
			}
			
			
			int type = data[0] & 0x0F;
			if(type > Type.values().length || type < 0) {
				return null;
			}
			Packet retval = new Packet();
			retval.mType = Type.values()[type];
			retval.mFromSocketAddress = new InetSocketAddress(datagram.getAddress(), datagram.getPort());
			retval.mData = new byte[datagram.getLength()];
			System.arraycopy(datagram.getData(), datagram.getOffset(), retval.mData, 0, retval.mData.length);
			
			//get from id
			retval.mFromId = new Id(retval.mData, 1);
			
			
			return retval;
		
		}
		return null;
	}
}