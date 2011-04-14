package xnet.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.IOBuffer;

/**
 * server，服务实例
 * 
 * @author quanwei
 * 
 */
public class Server {
	static Log logger = LogFactory.getLog(Server.class);

	Config config;
	Worker[] workers;
	Selector selector;
	ServerSocketChannel socket;
	int nextWorkerIndex = 0;

	public Server(Config config) {
		this.config = config;
		workers = new Worker[config.threadNum];
	}

	/**
	 * 启动服务
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		logger.debug("DEBUG ENTER");

		ExecutorService pool = Executors.newFixedThreadPool(config.threadNum);
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker();
			pool.execute(workers[i]);
		}

		logger.debug("DEBUG ENTER");
		for (int i = 0; i < config.maxConnection; i++) {
			Session session = (Session) config.session.newInstance();
			session.config = config;
			session.event = Session.EVENT_READ;
			session.inuse = false;
			session.readBuf = new IOBuffer();
			session.writeBuf = new IOBuffer();
			SessionPool.addSession(session);
		}

		logger.debug("DEBUG ENTER");
		initServerSocket();
		SocketChannel csocket = null;

		logger.debug("DEBUG ENTER");
		while (true) {
			selector.select();
			try {
				csocket = socket.accept();
				logger.debug("DEBUG ENTER");
				csocket.configureBlocking(false);
				Session session = SessionPool.openSession();
				if (session == null) {
					logger.warn("too many connection");
					csocket.close();
					continue;
				} else {
					session.socket = csocket;
					handleNewSession(session);
				}
			} catch (IOException e) {
				if (csocket != null && csocket.isConnected()) {
					csocket.close();
				}
				logger.warn(e);
			}
		}
	}

	/**
	 * 初始化server套接字
	 * 
	 * @throws IOException
	 */
	public void initServerSocket() throws IOException {
		logger.debug("DEBUG ENTER");

		selector = Selector.open();
		socket = ServerSocketChannel.open();
		socket.socket().setReuseAddress(true);
		socket.configureBlocking(false);
		socket.socket().bind(new InetSocketAddress(InetAddress.getByName(config.ip), config.port));
		socket.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * 处理一个新session，为其指定一个工作线程，并加入到工作线程新session队列
	 * 
	 * @param session
	 */
	private void handleNewSession(Session session) {
		logger.debug("DEBUG ENTER");

		workers[nextWorkerIndex].addNewSession(session);
		nextWorkerIndex = (nextWorkerIndex + 1) % config.threadNum;
	}

}
