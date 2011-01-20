package xnet.core;
   
import java.nio.channels.SocketChannel;

import xnet.server.Server;
import xnet.server.Worker;


public interface IConnection {	
	/**
	 * ���õ�ǰ�����߳�
	 * @param worker
	 */
	public void setWorker(Worker worker);
	/**
	 * ��ȡ��ǰ�����߳�
	 * @return
	 */
	public Worker getWorker();
	/**
	 * ���÷���
	 * @param worker
	 */
	public void setServer(Server server);
	/**
	 * ��ȡ����
	 * @return
	 */
	public Server getServer();
	/**
	 * ����use״̬
	 * @param isUse
	 */
	public void setUse(boolean use);
	/**
	 * ��ȡuse״̬
	 * @return
	 */
	public boolean isUse();
	/**
	 * �������Ӷ�Ӧ��SocketChannel
	 * @param socketChannel
	 */
	public void setSocketChannel(SocketChannel socketChannel);
	/**
	 * ��ȡ���Ӷ�Ӧ��SocketChannel
	 */
	public SocketChannel getSocketChannel();
	
	
	/**
	 * ���ӽ���
	 */
	public void connectionCreate();
	/**
	 * ������
	 */
	public void requestHandle();
	/**
	 * ���ӹر�
	 */
	public void connectionClose();
	
	
}