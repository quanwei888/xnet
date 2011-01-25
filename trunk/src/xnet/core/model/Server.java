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
import xnet.core.connection.IConnectionFactory;
import xnet.core.event.*;
import xnet.core.io.IOBuffer;

public class Server {
	static Log logger = LogFactory.getLog(Server.class);

	protected ServerSocketChannel socket = null;

	protected EventManager ev = null;
	/**
	 * 端口号
	 */
	protected int port = 8123;
	/**
	 * 读超时
	 */
	protected long readTimeout = 0;

	public int getPort() {
		return port;
	}

	public long getReadTimeout() {
		return readTimeout;
	}

	public long getWriteTimeout() {
		return writeTimeout;
	}

	public int getThreadNum() {
		return threadNum;
	}

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
	/**
	 * 长连接
	 */
	protected boolean keepalive = false;

	protected IOBuffer pipeBuf = new IOBuffer(1);

	/**
	 * 长连接
	 * 
	 * @return
	 */
	public boolean isKeepalive() {
		return keepalive;
	}

	/**
	 * 设置长连接
	 * 
	 * @param keepalive
	 */
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
		socket = ServerSocketChannel.open();
		socket.socket().setReuseAddress(true);
		socket.configureBlocking(false);
		socket.socket().bind(new InetSocketAddress(port));

		initThread();
		ev = new EventManager();
		ev.addEvent(socket, EventType.EV_ACCEPT | EventType.EV_PERSIST, new Server.ServerHandle(), this, 0);
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
				workers[lastThread % server.threadNum].sinkChannel.write(pipeBuf.getBuf());
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
