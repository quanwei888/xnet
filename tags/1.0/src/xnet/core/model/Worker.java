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
	 * 事件处理器
	 */
	IEventHandle evHandle;
	/**
	 * 当前服务
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
	 * 线程主方法
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
	 * 处理管道数据，表示新连接建立
	 * 
	 * @author quanwei
	 * 
	 */
	class PipeHandle implements IEventHandle {
		public void onIOEvent(SelectableChannel channel, int type, Object obj) {
			SourceChannel sourceChannel = (SourceChannel) channel;
			try {
				// 从管道读1byte
				pipeBuf.clear();
				sourceChannel.read(pipeBuf.getBuf());
				logger.debug("worker read from pipe");

				if (pipeBuf.remaining() != 0) {
					return;
				}

				// 从connection池中取出一个新连接
				IConnection conn = ConnectionPool.popQueue();
				if (conn == null) {
					return;
				}
				conn.setWorker((Worker) obj);
				conn.setServer(server);
				// 连接建立
				conn.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 在work线程中注册io事件
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
	 * 在work线程中删除io事件
	 * 
	 * @param socket
	 */
	public void delEvent(SocketChannel socket) {
		em.delEvent(socket);
	}
}
