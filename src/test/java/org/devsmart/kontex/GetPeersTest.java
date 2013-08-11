package org.devsmart.kontex;

import static org.mockito.Mockito.spy;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetPeersTest {

	Context context1;
	Context context2;
	
	@Before
	public void setupPeers() throws Exception {
		
		byte[] idData = new byte[Id.NUM_BYTES];
		
		context1 = spy(new Context());
		idData[0] = (byte) 0x80;
		Context.initialize(context1, 9000, new Id(idData));
		
		context2 = spy(new Context());
		idData[0] = (byte) 0x00;
		Context.initialize(context2, 9001, new Id(idData));
		
		
	}
	
	@Test
	public void testGetPeers() throws Exception {
		
		Random r = new Random(1);
		
		//load a bunch of random peers into context2
		Peer p = null;
		for(int i=0;i<10;i++){
			InetSocketAddress address = new InetSocketAddress("192.168.0."+(1+i), 2000+i);
			p = new Peer(address, Utils.randomId(r));
			context2.mPeerTable.addPeer(p);
		}
		
		context1.sendPacket(
				PacketFactory.createGetPeersRequestPacket(context1.mId, context2.mId, p.mId),
				new InetSocketAddress("localhost", 9001));
		
		Thread.sleep(3000);
		
		LinkedList<Peer> peers = context1.mPeerTable.getBucket(p.mId);
		Assert.assertTrue(!peers.isEmpty());
		Assert.assertTrue(peers.contains(p));
		
	}
}
