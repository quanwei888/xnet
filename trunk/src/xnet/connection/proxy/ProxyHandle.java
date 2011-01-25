package xnet.connection.proxy;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.event.*;
import xnet.core.model.*;

public abstract class ProxyHandle implements IEventHandle {
	public static long cReadTimeout = 0;
	public static long sReadTimeout = 0;
	public static long cWriteTimeout = 0;
	public static long sWriteTimeout = 0;

	protected static Log logger = LogFactory.getLog(ProxyHandle.class);

	/**
	 * client socket
	 */
	protected SocketChannel cSocket;
	/**
	 * server socket
	 */
	protected SocketChannel sSocket;
	protected Worker worker;
	protected Server server;
	protected ProxyEvent lastEvent;

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public SocketChannel getcSocket() {
		return cSocket;
	}

	public void setcSocket(SocketChannel cSocket) {
		this.cSocket = cSocket;
	}

	public SocketChannel getsSocket() {
		return sSocket;
	}

	public void setsSocket(SocketChannel sSocket) {
		this.sSocket = sSocket;
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public abstract void handleServer(SocketChannel socket, int type) throws Exception;

	public abstract void handleClient(SocketChannel socket, int type) throws Exception;

	public void closeClient() {
		if (cSocket != null && cSocket.isConnected()) {
			try {
				worker.getEv().delEvent(cSocket);
				cSocket.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
		}
	}

	public void closeServer() {
		if (sSocket != null && sSocket.isConnected()) {
			try {
				worker.getEv().delEvent(sSocket);
				sSocket.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
		}
	}

	public void close() {
		closeClient();
		closeServer();
	}

	public void onIOEvent(SelectableChannel socket, int type, Object obj) {
		try {
			if (socket == cSocket) {
				handleClient(cSocket, type);
			} else {
				handleServer(sSocket, type);
			}
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	public void handleConnection() {
		try {
			handleServer(sSocket, EventType.EV_CONNECT);
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}

	}

	protected void addEvent(SocketChannel socket, int type, long timeout) throws Exception {
		worker.getEv().addEvent(socket, type, this, null, timeout);
	}
}
