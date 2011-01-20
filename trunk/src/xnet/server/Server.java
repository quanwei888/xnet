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
	 * 端口号
	 */
	protected int port = 8123;
	/**
	 * 读超时
	 */
	protected long readTimeout = 0;
	/**
	 * 写超时
	 */
	protected long writeTimeout = 0;
	/**
	 * 工作线程数
	 */
	protected int threadNum = 8;
	/**
	 * 工作线程
	 */
	protected Worker[] workers;
	/**
	 * 上一次工作线程ID
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
	 * 设置端口号
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 设置读超时
	 * 
	 * @param readTimeout
	 */
	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * 设置写超时
	 * 
	 * @param writeTimeout
	 */
	public void setWriteTimeout(long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	/**
	 * 设置工作线程数
	 * 
	 * @param threadNum
	 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	/**
	 * 设置connection类型
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
	 * 初始化工作线程
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
	 * server事件处理器，用于处理accept事件
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
