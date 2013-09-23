package org.devsmart.kontex;

import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Utils {
	
	public final static ScheduledExecutorService sIOThreads = Executors.newScheduledThreadPool(1);
	
	private final static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static long getUptime() {
		return ManagementFactory.getRuntimeMXBean().getUptime();
	}
	
	public static Id randomId(Random r) {
		byte[] data = new byte[Id.NUM_BYTES];
		r.nextBytes(data);
		return new Id(data);
	}
	
	public static class PeerDistanceComparator implements Comparator<Peer> {

		public final Id mTarget;
		public PeerDistanceComparator(Id target){
			mTarget = target;
		}
		
		@Override
		public int compare(Peer a, Peer b) {
			BigInteger distanceToA = mTarget.distance(a.mId);
			BigInteger distanceToB = mTarget.distance(b.mId);
			
			int retval = distanceToA.compareTo(distanceToB);
			return retval;
		}
		
	}

}
