package xnet.core.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.io.IOBuffer;
import xnet.core.io.IOState;
import xnet.core.model.*;

public abstract class Connection implements IConnection {
	static Log logger = LogFactory.getLog(Connection.class); 

	/**
	 * 句柄
	 */
	protected SocketChannel socket;
	/**
	 * 当前工作线程
	 */
	protected Worker worker;
	/**
	 * 当前服务
	 */
	protected Server server;
	/**
	 * 是否正在使用
	 */
	protected boolean use = false;

	public SocketChannel getSocketChannel() {
		return socket;
	}

	public void setSocket(SocketChannel socketChannel) {
		this.socket = socketChannel;
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public boolean isUse() {
		return use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	/**
	 * 从网络读buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	protected IOState nread(SocketChannel socketChannel, IOBuffer readBuf) {
		try {
			int len = socketChannel.read(readBuf.getBuf());
			logger.debug("remdin:" + readBuf.remaining() + ",read len:" + len);
			if (len < 0) {
				return IOState.ERROR;
			}
			if (readBuf.remaining() == 0) {
				return IOState.COMPLATE;
			} else {
				return IOState.GOON;
			}
		} catch (IOException e) {
			return IOState.ERROR;
		}
	}

	/**
	 * 从网络写buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	protected IOState nwrite(SocketChannel socketChannel, IOBuffer writeBuf) {
		try {
			int len = socketChannel.write(writeBuf.getBuf());
			logger.debug("remdin:" + writeBuf.remaining() + ",write len:" + len);
			if (len < 0) {
				return IOState.ERROR;
			}
			if (writeBuf.remaining() == 0) {
				return IOState.COMPLATE;
			} else {
				return IOState.GOON;
			}
		} catch (IOException e) {
			return IOState.ERROR;
		}
	}
}
