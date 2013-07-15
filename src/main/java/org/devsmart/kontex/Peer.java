package org.devsmart.kontex;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Peer {
	
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
				
				context.sendPing(Peer.this);
				
				switch(getState()){
				case Dead:
					context.mRouter.removePeer(Peer.this);
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
	
	
	
}
