package xnet.connection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

public class ConnectionPool {
	static Log logger = LogFactory.getLog(ConnectionPool.class);
	
	public static IConnectionFactory connFactory;
	public static int maxNum = 5000;
	public static List<IConnection> connList = new ArrayList<IConnection>();
	public static Queue<IConnection> connQueue = new LinkedList<IConnection>();
	public static Lock lock = new ReentrantLock();
	public static Lock queueLock = new ReentrantLock();

	public static IConnection alloc() throws InstantiationException, IllegalAccessException {		
		IConnection ret = null;
		lock.lock();
		// 如果有空闲连接，则复用
		for (IConnection conn : connList) {
			if (!conn.isUse()) {
				conn.setUse(true);
				lock.unlock();
				logger.debug("alloc:reuse a connection");
				return conn;
			}
		}
		// 申请新连接
		if (connList.size() > maxNum) {
			lock.unlock();
			logger.warn("too many connection ");
			ret = null;
		} else {
			ret = connFactory.createConnection();
			ret.setUse(true);
			lock.unlock();
			connList.add(ret);
			logger.debug("alloc:new a connection");
		}		 
		return ret;
	}

	public static void free(IConnection conn) {
		logger.debug("free a connection");
		
		lock.lock();
		conn.setUse(false);
		lock.unlock();		
	}

	public static void pushQueue(IConnection conn) {
		logger.debug("push a connection");
		
		queueLock.lock();
		connQueue.add(conn);
		queueLock.unlock();
	}

	public static IConnection popQueue() {
		logger.debug("pop a connection");
		
		IConnection ret = null;
		queueLock.lock();
		if (connQueue.size() > 0) {
			ret = connQueue.poll();
		}
		queueLock.unlock();		
		return ret;
	}
}
