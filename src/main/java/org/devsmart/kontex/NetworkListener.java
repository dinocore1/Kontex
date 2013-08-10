package org.devsmart.kontex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkListener {

	public static final Logger sLogger = Logger.getLogger("UDP");
	
	protected static final int TIMEOUT_MILISEC = 1000;
	
	Context mContext;
	private Thread mThread;
	private boolean mRunning;

	DatagramSocket mSocket;

	public void start(InetSocketAddress address) throws SocketException {

		mSocket = new DatagramSocket(address);
		mSocket.setSoTimeout(TIMEOUT_MILISEC);
		
		stop();
		mThread = new Thread(new Runnable(){

			public void run() {
				try {
					mRunning = true;
					byte[] data = new byte[64 * 1024];
					while(mRunning){
						DatagramPacket packet = new DatagramPacket(data, data.length);
						try {
							mSocket.receive(packet);
							Packet goodPacket = Packet.parseDatagram(packet);
							if(goodPacket != null){
								mContext.onReciveDatagram(goodPacket);
							}
						} catch (SocketTimeoutException timeout) {}
						
					}
				} catch(IOException e){
					sLogger.log(Level.SEVERE, "unhandled IOException, shutting down socket listener", e);
				} finally {
					mRunning = false;
				}
			}

		}, "Listening " + address);
		mThread.start();
	}

	public void stop() {
		mRunning = false;
		try {
			if(mThread != null){
				mThread.join();
			}
			mThread = null;

		} catch (InterruptedException e) {
			sLogger.log(Level.WARNING, "unhandled exception", e);
		}
	}
	
	public void sendPacket(Packet packet, InetSocketAddress address) {
		DatagramPacket dgram = new DatagramPacket(packet.mData, packet.mData.length, address.getAddress(), address.getPort());
		try {
			mSocket.send(dgram);
		} catch (IOException e) {
			sLogger.log(Level.WARNING, "exception when sending UDP packet", e);
		}
	}
}
