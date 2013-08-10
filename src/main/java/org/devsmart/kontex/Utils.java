package org.devsmart.kontex;

import java.lang.management.ManagementFactory;
import java.util.Random;

public class Utils {
	
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

}
