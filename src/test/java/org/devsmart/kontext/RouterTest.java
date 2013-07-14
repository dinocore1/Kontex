package org.devsmart.kontext;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class RouterTest {
	
	Context mContext;
	
	@Before
	public void setupRouter() {
		mContext = Context.createContext();
		
	}

	@Test
	public void getBucketTest() {
		
		byte[] idData = new byte[Id.NUM_BYTES];
		idData[0] = (byte) 0xA8;
		mContext.mId = new Id(idData);
		
		
		idData[0] = (byte) 0x00;
		Peer p = new Peer();
		p.mId = new Id(idData);
		LinkedList<Peer> bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(0));
		
		idData[0] = (byte) 0x80;
		p = new Peer();
		p.mId = new Id(idData);
		bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(2));
		
		idData[0] = (byte) 0xA4;
		p = new Peer();
		p.mId = new Id(idData);
		bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(4));
		
		idData[0] = (byte) 0xAF;
		p = new Peer();
		p.mId = new Id(idData);
		bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(5));
		
		idData[0] = (byte) 0xAE;
		p = new Peer();
		p.mId = new Id(idData);
		bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(5));
		
		idData[0] = (byte) 0xA8;
		idData[1] = (byte) 0x80;
		p = new Peer();
		p.mId = new Id(idData);
		bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(9));
		
		idData[0] = (byte) 0xA8;
		idData[1] = (byte) 0x40;
		p = new Peer();
		p.mId = new Id(idData);
		bucket = mContext.mRouter.getBucket(p);
		assertEquals(bucket, mContext.mRouter.mTable.get(10));
		
	}

}
