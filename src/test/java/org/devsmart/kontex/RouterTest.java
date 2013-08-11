package org.devsmart.kontex;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class RouterTest {
	
	Context mContext;
	
	@Before
	public void setupRouter() throws Exception {
		mContext = Context.createContext();
		
	}

	@Test
	public void getBucketTest() {
		
		byte[] idData = new byte[Id.NUM_BYTES];
		idData[0] = (byte) 0xA8;
		mContext.mId = new Id(idData);
		
		
		idData[0] = (byte) 0x00;
		LinkedList<Peer> bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(0));
		
		idData[0] = (byte) 0x80;
		bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(2));
		
		idData[0] = (byte) 0xA4;
		bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(4));
		
		idData[0] = (byte) 0xAF;
		bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(5));
		
		idData[0] = (byte) 0xAE;
		bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(5));
		
		idData[0] = (byte) 0xA8;
		idData[1] = (byte) 0x80;
		bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(9));
		
		idData[0] = (byte) 0xA8;
		idData[1] = (byte) 0x40;
		bucket = mContext.mPeerTable.getBucket(new Id(idData));
		assertEquals(bucket, mContext.mPeerTable.mTable.get(10));
		
	}

}
