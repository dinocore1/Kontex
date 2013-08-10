package org.devsmart.kontex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import org.devsmart.kontex.bencode.BEValue;

public class Id implements Comparable<Id>, BEncodable {

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
	
	public void write(byte[] buffer, int offset){
		System.arraycopy(mIdBytes, 0, buffer, offset, NUM_BYTES);
	}
	
	public void write(OutputStream output) throws IOException{
		output.write(mIdBytes);
	}
	
	public void read(InputStream input) throws IOException {
		int bytesToRead = NUM_BYTES;
		while(bytesToRead > 0){
			bytesToRead -= input.read(mIdBytes, NUM_BYTES - bytesToRead, bytesToRead);
		}
		
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

	public int compareTo(Id o) {
		int retval = 0;
		for(int i=0;i<NUM_BYTES;i++){
			retval = ((int)(mIdBytes[i] & 0xFF)) - ((int)(o.mIdBytes[i] & 0xFF));
			if(retval != 0){
				break;
			}
		}
		return retval;
	}

	@Override
	public BEValue encode() throws IOException {
		BEValue retval = new BEValue(mIdBytes);
		return retval;
	}

	@Override
	public void decode(BEValue value) throws IOException {
		mIdBytes = value.getBytes();
	}


	
	
}
