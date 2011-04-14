package xnet.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * session池
 * 
 * @author quanwei
 * 
 */
public class SessionPool {
	static Log logger = LogFactory.getLog(SessionPool.class);
	/**
	 * session池
	 */
	public static List<Session> sessionList = new ArrayList<Session>();
	static Boolean lock = false;

	/**
	 * 从session池中打开一个session 新连接建立时调用
	 * 
	 * @return
	 */
	public static Session openSession() {
		logger.debug("DEBUG ENTER");

		synchronized (lock) {
			Iterator<Session> sessionIter = sessionList.iterator();
			while (sessionIter.hasNext()) {
				Session session = sessionIter.next();
				if (!session.inuse) {
					session.inuse = true;
					logger.debug("session id:" + session);
					return session;
				}
			}
		}
		return null;
	}

	/**
	 * 关闭一个session，并放入session池中供后续复用
	 * 
	 * @param session
	 */
	public static void closeSession(Session session) {
		logger.debug("DEBUG ENTER");
		synchronized (lock) {
			session.inuse = false;
		}
	}

	/**
	 * 添加一个新session到session池
	 * 
	 * @param session
	 */
	public static void addSession(Session session) {
		sessionList.add(session);
	}
}
