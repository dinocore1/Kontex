package org.devsmart.kontex;

import java.math.BigInteger;
import java.util.Arrays;

public class Id {

	public static final int NUM_BYTES = 20;
	
	protected byte[] mIdBytes = new byte[NUM_BYTES];
	
	public Id() {
		
	}
	
	public Id(byte[] data, int offset) {
		for(int i=0;i<Math.min(data.length-offset, NUM_BYTES);i++){
			mIdBytes[i] = data[offset+i];
		}
	}
	
	public Id(byte[] data) {
		for(int i=0;i<Math.min(NUM_BYTES, data.length);i++){
			mIdBytes[i] = data[i];
		}
	}
	
	public Id(Id copy){
		System.arraycopy(copy.mIdBytes, 0, mIdBytes, 0, NUM_BYTES);
	}
	
	public BigInteger distance(Id other) {
		byte[] resultBytes = new byte[NUM_BYTES];
		for(int i=0;i<NUM_BYTES;i++){
			resultBytes[i] = (byte) (mIdBytes[i] ^ other.mIdBytes[i]);
		}
		return new BigInteger(1, resultBytes);
	}
	
	@Override
	public String toString() {
		return "["+Utils.bytesToHex(mIdBytes) + "]";
	}

	@Override
	public boolean equals(Object obj) {
		boolean retval = false;
		if(obj instanceof Id){
			retval = Arrays.equals(mIdBytes, ((Id) obj).mIdBytes);
		}
		return retval;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(mIdBytes);
	}
	
	
}
