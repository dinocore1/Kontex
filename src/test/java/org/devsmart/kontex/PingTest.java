package org.devsmart.kontex;

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PingTest {

	Context context1;
	Context context2;
	
	@Before
	public void setupPeers() throws Exception {
		
		byte[] idData = new byte[Id.NUM_BYTES];
		idData[0] = (byte) 0x80;
		context1 = Context.createContext(9000, new Id(idData));
		
		context2 = spy(new Context());
		idData[0] = (byte) 0x00;
		Context.initialize(context2, 9001, new Id(idData));
		
		
	}
	
	@Test
	public void testPing() throws InterruptedException {
		
		
		Peer peer = new Peer(new InetSocketAddress(9001), new Id());
		context1.sendPing(peer);
		
		Thread.sleep(3000);
		
		verify(context2).onReciveDatagram(any(Packet.class));
		
		
	}
}
