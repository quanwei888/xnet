package xnet.core.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.util.IOBuffer;
import xnet.core.util.NetIo;

/**
 * 工作线程
 * 
 * @author quanwei
 * 
 */
public class Worker implements Runnable {
	static Log logger = LogFactory.getLog(Worker.class);

	Selector selector;
	Boolean lock = false;
	
	/**
	 * 新进来的session队列
	 */
	Queue<Session> newSessionQueue = new ConcurrentLinkedQueue<Session>();
	/**
	 * 超时的session集合， TreeSet保证集合中的session按超时时间升序排列
	 */
	Set<Session> timeoutSessionSet = new TreeSet<Session>(new Comparator<Session>() {
		public int compare(Session a, Session b) {
			if (a.nextTimeout - b.nextTimeout == 0) {
				return 0;
			} else if (a.nextTimeout - b.nextTimeout > 0) {
				return 1;
			} else {
				return -1;
			}
		}
	});
	/**
	 * 有IO事件或超时的session队列
	 */
	List<Session> eventSessionList = new ArrayList<Session>();

	public Worker() throws IOException {
		selector = Selector.open();
	}

	/**
	 * 设置超时的session加入到超时队列中
	 * 
	 * @param session
	 */
	public void addTimeSession(Session session) {
		logger.debug("DEBUG ENTER");

		if (session.nextTimeout > 0) {
			timeoutSessionSet.add(session);
		}
	}

	/**
	 * 起哦的那个工作线程
	 */
	public void run() {
		logger.debug("DEBUG ENTER");

		try {
			dispatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 轮询调度非阻塞请求
	 * 
	 * @throws IOException
	 */
	public void dispatch() throws IOException {
		logger.debug("DEBUG ENTER");

		while (true) {
			select();
		}
	}

	/**
	 * 执行一次调度
	 * 
	 * @throws IOException
	 */
	public void select() throws IOException {
		logger.debug("DEBUG ENTER");

		long timeout = 0;
		Iterator<Session> sessionIter = timeoutSessionSet.iterator();
		if (sessionIter.hasNext()) {
			timeout = sessionIter.next().nextTimeout - System.currentTimeMillis();
			timeout = Math.max(timeout, 1);
		}
		logger.debug("time out:" + timeout);
		selector.select(timeout);
		long now = System.currentTimeMillis();

		// 处理新来的session
		handleNewSession();
		// 检查有IO或超时session
		checkEventSession(now);
		// 处理session事件
		handleEventSession();
	}

	/**
	 * 处理新增连接
	 */
	public void handleNewSession() {
		logger.debug("DEBUG ENTER");

		while (true) {
			Session session = newSessionQueue.poll();
			if (session == null) {
				break;
			}
			logger.debug("DEBUG ENTER");
			initNewSession(session);
		}
	}

	/**
	 * 为工作线程增加一个session，server线程中调用
	 * 
	 * @param session
	 */
	public void addNewSession(Session session) {
		logger.debug("DEBUG ENTER");

		newSessionQueue.add(session);
		// 唤醒阻塞中的select
		selector.wakeup();
	}

	/**
	 * 初始化新增连接
	 * 
	 * @param session
	 */
	public void initNewSession(Session session) {
		logger.debug("DEBUG ENTER");

		try {
			// 初始化buffer
			session.readBuf.position(0);
			session.readBuf.limit(0);
			session.writeBuf.position(0);
			session.writeBuf.limit(0);

			// 链接建立回调函数
			session.open(session.readBuf, session.writeBuf);
			// 更新session
			updateSession(session);
		} catch (Exception e) {
			close(session);
			e.printStackTrace();
		}

	}

	/**
	 * 当执行回调函数后更新session
	 * 
	 * @param session
	 * @throws ClosedChannelException
	 */
	private void updateSession(Session session) throws ClosedChannelException {
		// 根据session状态注册读或写事件
		if (session.state == Session.STATE_READ && session.readBuf.remaining() > 0) {
			if (session.config.rTimeout > 0) {
				session.nextTimeout = System.currentTimeMillis() + session.config.rTimeout;
			}
			session.socket.register(selector, SelectionKey.OP_READ, session);

			// 加入到超时session队列
			addTimeSession(session);
		} else if (session.state == Session.STATE_WRITE && session.writeBuf.remaining() > 0) {
			if (session.config.wTimeout > 0) {
				session.nextTimeout = System.currentTimeMillis() + session.config.wTimeout;
			}
			session.socket.register(selector, SelectionKey.OP_WRITE, session);

			// 加入到超时session队列
			addTimeSession(session);
		} else {
			close(session);
		}
	}

	/**
	 * 检测有IO或超时事件的session，并加入到eventSessionList中
	 * 
	 * @param now
	 */
	private void checkEventSession(long now) {
		logger.debug("DEBUG ENTER");

		eventSessionList.clear();
		// 有IO事件的session
		Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
		while (keyIter.hasNext()) {
			SelectionKey key = keyIter.next();
			keyIter.remove();

			Session session = (Session) key.attachment();
			session.event = session.state == Session.EVENT_READ ? Session.EVENT_READ : Session.EVENT_WRITE;

			eventSessionList.add(session);
			timeoutSessionSet.remove(session);
		}

		// 有超时的session
		Iterator<Session> sessionIter = timeoutSessionSet.iterator();
		while (sessionIter.hasNext()) {
			Session session = sessionIter.next();
			if (session.nextTimeout <= now) {
				session.event = Session.EVENT_TIMEOUT;
				eventSessionList.add(session);
				sessionIter.remove();
			} else {
				break;
			}
		}

	}

	/**
	 * 处理当前有事件session 3种事件：可读、可写、超时
	 */
	private void handleEventSession() {
		logger.debug("DEBUG ENTER");

		// 处理事件
		Iterator<Session> eventIter = eventSessionList.iterator();
		while (eventIter.hasNext()) {
			Session session = eventIter.next();
			logger.debug(session.event);
			try {
				if (session.event == Session.EVENT_TIMEOUT) {
					timeoutEvent(session);
				} else if (session.state == Session.STATE_READ) {
					readEvent(session);
				} else if (session.state == Session.STATE_WRITE) {
					writeEvent(session);
				}
			} catch (Exception e) {
				eventIter.remove();
				close(session);
			}
		}
	}

	/**
	 * 处理超时事件
	 * 
	 * @param session
	 * @throws Exception
	 */
	private void timeoutEvent(Session session) {
		logger.debug("DEBUG ENTER");

		try {
			session.timeout(session.readBuf, session.writeBuf);
			updateSession(session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ioEvent(Session session, IOBuffer buf) throws Exception {
		logger.debug("DEBUG ENTER");

		while (buf.remaining() > 0) {
			int len = 0;
			int curState = session.state;
			if (curState == Session.STATE_READ) {
				len = NetIo.read(session.socket, buf);// 读数据
			} else {
				len = NetIo.write(session.socket, buf);// 读数据
			}
			int remain = buf.remaining();// 剩余字节数

			if (curState == Session.STATE_READ) {
				if (remain == 0) {
					// 当前数据IO完成
					session.complateRead(session.readBuf, session.writeBuf);
				} else {
					// 当前数据IO未完成
					session.complateReadOnce(session.readBuf, session.writeBuf);
				}
			} else {
				if (remain == 0) {
					// 当前数据IO完成
					session.complateWrite(session.readBuf, session.writeBuf);
				} else {
					// 当前数据IO未完成
					session.complateWriteOnce(session.readBuf, session.writeBuf);
				}
			}

			if (len == 0 || session.state != curState) {
				// 不可IO或者session状态切换
				updateSession(session);
				break;
			}
		}
	}

	/**
	 * 处理读事件
	 * 
	 * @param session
	 * @throws Exception
	 */
	public void readEvent(Session session) throws Exception {
		logger.debug("DEBUG ENTER");

		ioEvent(session, session.readBuf);
	}

	/**
	 * 处理写事件
	 * 
	 * @param session
	 * @throws ClosedChannelException
	 */
	public void writeEvent(Session session) throws Exception {
		logger.debug("DEBUG ENTER");

		ioEvent(session, session.writeBuf);
	}

	/**
	 * 关闭session 用于清理
	 * 
	 * @param session
	 */
	protected void close(Session session) {
		logger.debug("DEBUG ENTER");

		if (session.socket != null) {
			try {
				if (session.socket.keyFor(selector) != null) {
					session.socket.keyFor(selector).cancel();
				}
				if (session.socket.isConnected()) {
					session.socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			session.socket = null;
		}
		timeoutSessionSet.remove(session);
		SessionPool.closeSession(session);
		// 执行close回调函数
		session.close();
	}

}
