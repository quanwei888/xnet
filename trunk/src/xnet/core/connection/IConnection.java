package xnet.core.connection;

import java.nio.channels.SocketChannel;

import xnet.core.model.Server;
import xnet.core.model.Worker;

public interface IConnection {
	/**
	 * ���õ�ǰ�����߳�
	 * 
	 * @param worker
	 */
	public void setWorker(Worker worker);

	/**
	 * ��ȡ��ǰ�����߳�
	 * 
	 * @return
	 */
	public Worker getWorker();

	/**
	 * ���÷���
	 * 
	 * @param worker
	 */
	public void setServer(Server server);

	/**
	 * ��ȡ����
	 * 
	 * @return
	 */
	public Server getServer();

	/**
	 * ����use״̬
	 * 
	 * @param isUse
	 */
	public void setUse(boolean use);

	/**
	 * ��ȡuse״̬
	 * 
	 * @return
	 */
	public boolean isUse();

	/**
	 * �������Ӷ�Ӧ��SocketChannel
	 * 
	 * @param socket
	 */
	public void setSocket(SocketChannel socket);

	/**
	 * ��ȡ���Ӷ�Ӧ��SocketChannel
	 */
	public SocketChannel getSocketChannel();

	/**
	 * ���ӽ���
	 */
	public void execute();

}
