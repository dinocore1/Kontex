package org.devsmart.kontex;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public class PingTest {

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
	public void testPing() throws InterruptedException {
		
		/*
		doAnswer(new Answer<Object>(){

			public Object answer(InvocationOnMock invocation) throws Throwable {
				Packet p = (Packet) invocation.getArguments()[0];
				Assert.assertNotNull(p);
				invocation.callRealMethod();
				return null;
			}
			
		}).when(context1).onReciveDatagram(any(Packet.class));
		*/
		
		Peer peer = new Peer(new InetSocketAddress(9001), new Id());
		context1.sendPing(peer);
		
		Thread.sleep(3000);
		
		Peer otherPeer = context1.mRouter.getBucket(context2.mId).getFirst();
		Assert.assertNotNull(otherPeer);
		Assert.assertTrue(otherPeer.equals(context2.mId));
		
		otherPeer = context2.mRouter.getBucket(context1.mId).getFirst();
		Assert.assertNotNull(otherPeer);
		Assert.assertTrue(otherPeer.equals(context1.mId));
		
		verify(context2).onReciveDatagram(any(Packet.class));
		verify(context1).onReciveDatagram(any(Packet.class));
		
		
	}
}
