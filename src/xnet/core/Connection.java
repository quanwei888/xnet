package xnet.core;

import java.nio.channels.SocketChannel;

import xnet.server.Server;
import xnet.server.Worker;


public abstract class Connection implements IConnection {
	/**
	 * 句柄
	 */
	protected SocketChannel socketChannel;
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
		return socketChannel;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
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

}
