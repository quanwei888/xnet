package xnet.server;

import java.io.IOException;
import java.net.InetSocketAddress; 
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import xnet.core.*;
import xnet.core.event.*;
import xnet.io.IOBuffer;
import xnet.test.Test;


public class Server {
	static Log logger = LogFactory.getLog(Server.class);
	
	protected ServerSocketChannel serverSocketChannel = null;
	protected EventManager ev = null;
	/**
	 * �˿ں�
	 */
	protected int port = 8123;
	/**
	 * ����ʱ
	 */
	protected long readTimeout = 0;
	/**
	 * д��ʱ
	 */
	protected long writeTimeout = 0;
	/**
	 * �����߳���
	 */
	protected int threadNum = 8;
	/**
	 * �����߳�
	 */
	protected Worker[] workers;
	/**
	 * ��һ�ι����߳�ID
	 */
	protected int lastThread = -1;

	protected boolean keepalive = false;
	
	protected IOBuffer pipeBuf = new IOBuffer(1);

	public boolean isKeepalive() {
		return keepalive;
	}

	public void setKeepalive(boolean keepalive) {
		this.keepalive = keepalive;
	}

	/**
	 * ���ö˿ں�
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * ���ö���ʱ
	 * 
	 * @param readTimeout
	 */
	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * ����д��ʱ
	 * 
	 * @param writeTimeout
	 */
	public void setWriteTimeout(long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	/**
	 * ���ù����߳���
	 * 
	 * @param threadNum
	 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	/**
	 * ����connection����
	 * 
	 * @param connCls
	 */
	public void setConnectionFactory(IConnectionFactory connFactory) {
		ConnectionPool.connFactory = connFactory;
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().setReuseAddress(true);
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.socket().bind(new InetSocketAddress(port));

		initThread();
		ev = new EventManager();
		ev.addEvent(serverSocketChannel, EventType.EV_ACCEPT | EventType.EV_PERSIST, new Server.ServerHandle(), this, 0);
		ev.loop();
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
			workers[i].sourceChannel = pipe.source();
			workers[i].sinkChannel = pipe.sink();
			workers[i].server = this;
			workers[i].sourceChannel.configureBlocking(false);
			workers[i].sinkChannel.configureBlocking(false);
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
		public void onIOReady(SelectableChannel select, int type, Object obj) {			 
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) select;
			SocketChannel socketChannel = null;
			Server server = (Server)obj;
			try {
				socketChannel = serverSocketChannel.accept();
				logger.debug("\n---------------------\nserver accetp," + socketChannel);
				IConnection conn = ConnectionPool.alloc();
				if (conn == null) {
					logger.debug("too many connection");
					return;
				}				 
				//logger.debug(socketChannel + conn.toString());
				socketChannel.configureBlocking(false);
				
				conn.setSocketChannel(socketChannel);
				ConnectionPool.pushQueue(conn);

				lastThread++;
				pipeBuf.clear();
				workers[lastThread % server.threadNum].sinkChannel.write(pipeBuf.getBuf());
			} catch (Exception e) {
				try {
					if (socketChannel != null) {
						socketChannel.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
}
