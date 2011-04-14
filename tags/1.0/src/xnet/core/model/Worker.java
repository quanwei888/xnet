package xnet.core.model;

import java.io.IOException;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.connection.ConnectionPool;
import xnet.core.connection.IConnection;
import xnet.core.event.*;
import xnet.core.io.IOBuffer;

public class Worker implements Runnable {
	static Log logger = LogFactory.getLog(Worker.class);
	EventManager em = null;
	/**
	 * read pipe channel
	 */
	SourceChannel sourceSocket = null;
	/**
	 * write pipe channel
	 */
	SinkChannel sinkSocket = null;
	/**
	 * �¼�������
	 */
	IEventHandle evHandle;
	/**
	 * ��ǰ����
	 */
	Server server;

	IOBuffer pipeBuf = new IOBuffer(1);

	public SinkChannel getSinkChannel() {
		return sinkSocket;
	}

	public Server getServer() {
		return server;
	}

	/**
	 * �߳�������
	 */
	public void run() {
		try {
			em = new EventManager();
			em.addEvent(sourceSocket, EventType.EV_READ | EventType.EV_PERSIST, new Worker.PipeHandle(), this, 0);
			em.loop();
		} catch (IOException e) {
			logger.warn(e.getStackTrace());
		}
	}

	/**
	 * ����ܵ����ݣ���ʾ�����ӽ���
	 * 
	 * @author quanwei
	 * 
	 */
	class PipeHandle implements IEventHandle {
		public void onIOEvent(SelectableChannel channel, int type, Object obj) {
			SourceChannel sourceChannel = (SourceChannel) channel;
			try {
				// �ӹܵ���1byte
				pipeBuf.clear();
				sourceChannel.read(pipeBuf.getBuf());
				logger.debug("worker read from pipe");

				if (pipeBuf.remaining() != 0) {
					return;
				}

				// ��connection����ȡ��һ��������
				IConnection conn = ConnectionPool.popQueue();
				if (conn == null) {
					return;
				}
				conn.setWorker((Worker) obj);
				conn.setServer(server);
				// ���ӽ���
				conn.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ��work�߳���ע��io�¼�
	 * 
	 * @param socket
	 * @param type
	 * @param proxyHandle
	 * @param timeout
	 */
	public void addEvent(SocketChannel socket, int type, IEventHandle proxyHandle, long timeout) {
		em.addEvent(socket, type, proxyHandle, proxyHandle, timeout);
	}

	/**
	 * ��work�߳���ɾ��io�¼�
	 * 
	 * @param socket
	 */
	public void delEvent(SocketChannel socket) {
		em.delEvent(socket);
	}
}
