package org.devsmart.kontex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.devsmart.kontex.bencode.BEValue;
import org.devsmart.kontex.bencode.BEncoder;

public class PacketFactory {
	
	public static Packet createPingPacket(Id from, Id to){
		Packet retval = new Packet();
		retval.mData = new byte[44];
		retval.mFrom = from;
		from.write(retval.mData, 0);
		retval.mTo = to;
		to.write(retval.mData, 20);
		retval.setType(Packet.TYPE_KEEPALIVE);
		retval.writeChecksum();
		return retval;
	}
	
	public static Packet createPongPacket(Id from, Id to) {
		Packet retval = new Packet();
		retval.mData = new byte[44];
		retval.mFrom = from;
		from.write(retval.mData, 0);
		retval.mTo = to;
		to.write(retval.mData, 20);
		retval.setType(Packet.TYPE_KEEPALIVE);
		retval.setAck();
		retval.writeChecksum();
		return retval;
	}
	
	public static Packet createGetPeersRequestPacket(Id from, Id to, Id target) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		from.write(output);
		to.write(output);
		output.write(new byte[4]);
		
		target.write(output);
		
		Packet retval = new Packet();
		retval.mData = output.toByteArray();
		retval.mFrom = from;
		retval.mTo = to;
		retval.setType(Packet.TYPE_GETPEERS);
		retval.writeChecksum();
		return retval;
	}

	public static Packet createGetPeersResponsePacket(Id from, Id to, List<Peer> peerList) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		from.write(output);
		to.write(output);
		output.write(new byte[4]);
		
		List<Object> values = new LinkedList<Object>();
		for(Peer p : peerList) {
			values.add(p.encode().getValue());
		}
		BEncoder.bencode(values, output);
		
		Packet retval = new Packet();
		retval.mData = output.toByteArray();
		retval.mFrom = from;
		retval.mTo = to;
		retval.setType(Packet.TYPE_GETPEERS);
		retval.setAck();
		retval.writeChecksum();
		return retval;
	}

}
