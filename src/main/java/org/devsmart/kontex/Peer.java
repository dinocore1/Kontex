package org.devsmart.kontex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.devsmart.kontex.bencode.BDecoder;
import org.devsmart.kontex.bencode.BEValue;
import org.devsmart.kontex.bencode.BEncoder;

public class Peer implements Comparable<Peer>, BEncodable{

	public final int DIEING_TIMEOUT = 30 * 1000;
	public final int DEAD_TIMEOUT = DIEING_TIMEOUT * 2;

	public static enum State {
		Alive,
		Dieing,
		Dead
	};

	protected InetSocketAddress mAddress;
	protected Id mId;
	protected long mLastSeen;

	private ScheduledFuture<?> mMaintainceTask;

	public Peer() {

	}

	public Peer(InetSocketAddress address, Id id) {
		mAddress = address;
		mId = id;
	}


	public State getState() {
		long timeout = Utils.getUptime() - mLastSeen;
		if(timeout > DEAD_TIMEOUT) {
			return State.Dead;
		} else if(timeout > DIEING_TIMEOUT) {
			return State.Dieing;
		} else {
			return State.Alive;
		}
	}

	public void startMaintance(final Context context) {
		stopMaintance();
		mMaintainceTask = context.mMainThread.scheduleWithFixedDelay(new Runnable() {
			public void run() {

				switch(getState()){
				case Dead:
					context.mRouter.removePeer(Peer.this);
					break;

				default:
					Packet pingPacket = PacketFactory.createPingPacket(context.mId, mId);
					context.sendPacket(pingPacket, mAddress);
					break;
				}

			}
		}, 10, 10, TimeUnit.SECONDS);

	}

	public void stopMaintance() {
		if(mMaintainceTask != null){
			mMaintainceTask.cancel(false);
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean retval = false;
		if(obj instanceof Peer){
			retval = mId.equals(((Peer) obj).mId) && mAddress.equals(((Peer) obj).mAddress);
		} else if(obj instanceof Id){
			retval = mId.equals(obj);
		} else if(obj instanceof InetSocketAddress){
			retval = mAddress.equals(obj);
		}
		return retval;
	}

	@Override
	public int hashCode() {
		return mAddress.hashCode() ^ mId.hashCode();
	}

	@Override
	public String toString() {
		return mAddress.toString() + ":" + mId.toString().substring(0, 4);
	}

	public int compareTo(Peer other) {
		return mId.compareTo(other.mId);
	}

	@Override
	public BEValue encode() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BEncoder.bencode(mAddress.getAddress().getAddress(), output);
		BEncoder.bencode(mAddress.getPort(), output);
		BEncoder.bencode(mId.mIdBytes, output);
		output.flush();
		BEValue retval = new BEValue(output.toByteArray());
		output.close();
		return retval;
	}
	
	@Override
	public void decode(final BEValue value) throws IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
		
		BEValue v = BDecoder.bdecode(input);
		InetAddress address = InetAddress.getByAddress(v.getBytes());
		v = BDecoder.bdecode(input);
		int port = v.getNumber().intValue();
		v = BDecoder.bdecode(input);
		Id id = new Id(v.getBytes());
		
		mAddress = new InetSocketAddress(address, port);
		mId = id;
	}

}
