package xnet.core.model;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.connection.ConnectionPool;
import xnet.core.connection.IConnection; 
import xnet.core.event.*;
import xnet.core.io.IOBuffer;

public class Server {
	static Log logger = LogFactory.getLog(Server.class);

	ServerSocketChannel socket;
	EventManager em = null;
	/**
	 * �˿ں�
	 */
	protected int port = 8123;

	/**
	 * ���ö˿ں�
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * �����߳���
	 */
	int threadNum = 8;

	/**
	 * ���ù����߳���
	 * 
	 * @param threadNum
	 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	/**
	 * �����߳�
	 */
	Worker[] workers;
	/**
	 * ��һ�ι����߳�ID
	 */
	int lastThread = -1;
	IOBuffer pipeBuf = new IOBuffer(1);

	/**
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		socket = ServerSocketChannel.open();
		socket.socket().setReuseAddress(true);
		socket.configureBlocking(false);
		socket.socket().bind(new InetSocketAddress(port));

		initThread();
		em = new EventManager();
		em.addEvent(socket, EventType.EV_ACCEPT | EventType.EV_PERSIST, new Server.ServerHandle(), this, 0);
		em.loop();
	}

	/**
	 * ��ʼ�������߳�
	 * 
	 * @throws IOException
	 */
	void initThread() throws IOException {
		workers = new Worker[threadNum];

		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker();
			Pipe pipe = Pipe.open();
			workers[i].sourceSocket = pipe.source();
			workers[i].sinkSocket = pipe.sink();
			workers[i].server = this;
			workers[i].sourceSocket.configureBlocking(false);
			workers[i].sinkSocket.configureBlocking(false);
			new Thread(workers[i]).start();
		}
	}

	/**
	 * server�¼������������ڴ���accept�¼�
	 * 
	 * @author quanwei
	 * 
	 */
	class ServerHandle implements IEventHandle {
		public void onIOEvent(SelectableChannel socket, int type, Object obj) {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) socket;
			SocketChannel csocket = null;
			Server server = (Server) obj;
			try {
				csocket = serverSocketChannel.accept();
				logger.debug("server accetp," + csocket);
				IConnection conn = ConnectionPool.alloc();
				if (conn == null) {
					logger.debug("too many connection");
					return;
				}

				csocket.configureBlocking(false);
				conn.setSocket(csocket);
				ConnectionPool.pushQueue(conn);

				lastThread++;
				pipeBuf.clear();
				workers[lastThread % server.threadNum].sinkSocket.write(pipeBuf.getBuf());
			} catch (Exception e) {
				logger.warn(e.getStackTrace());
				try {
					if (csocket != null) {
						csocket.close();
					}
				} catch (IOException e1) {
					logger.warn(e.getStackTrace());
				}
			}
		}
	}
}
