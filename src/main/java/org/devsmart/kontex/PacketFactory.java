package org.devsmart.kontex;

public class PacketFactory {
	
	public static Packet createPingPacket(Id from, Id to){
		Packet retval = new Packet();
		retval.mData = new byte[44];
		retval.mFrom = from;
		retval.mTo = to;
		retval.setType(Packet.TYPE_KEEPALIVE);
		return retval;
	}
	
	public static Packet createPongPacket(Id from, Id to) {
		Packet retval = new Packet();
		retval.mData = new byte[44];
		retval.mFrom = from;
		retval.mTo = to;
		retval.setType(Packet.TYPE_KEEPALIVE);
		retval.setAck();
		return retval;
	}

}
