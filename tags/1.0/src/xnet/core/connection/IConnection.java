package xnet.core.connection;

import java.nio.channels.SocketChannel;

import xnet.core.model.Server;
import xnet.core.model.Worker;

public interface IConnection {
	/**
	 * 设置当前工作线程
	 * 
	 * @param worker
	 */
	public void setWorker(Worker worker);

	/**
	 * 获取当前工作线程
	 * 
	 * @return
	 */
	public Worker getWorker();

	/**
	 * 设置服务
	 * 
	 * @param worker
	 */
	public void setServer(Server server);

	/**
	 * 获取服务
	 * 
	 * @return
	 */
	public Server getServer();

	/**
	 * 设置use状态
	 * 
	 * @param isUse
	 */
	public void setUse(boolean use);

	/**
	 * 获取use状态
	 * 
	 * @return
	 */
	public boolean isUse();

	/**
	 * 设置连接对应的SocketChannel
	 * 
	 * @param socket
	 */
	public void setSocket(SocketChannel socket);

	/**
	 * 获取连接对应的SocketChannel
	 */
	public SocketChannel getSocketChannel();

	/**
	 * 连接建立
	 */
	public void execute();

}
