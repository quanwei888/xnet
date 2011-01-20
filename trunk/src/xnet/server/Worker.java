package xnet.server;

import java.io.IOException;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectableChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import xnet.core.*;
import xnet.core.event.*;
import xnet.io.IOBuffer;
import xnet.test.Test;


public class Worker implements Runnable {
	static Log logger = LogFactory.getLog(Worker.class);
	
	/**
	 * 事件管理器
	 */
	protected EventManager ev = null;
	/**
	 * read pipe channel
	 */
	protected SourceChannel sourceChannel = null;
	/**
	 * write pipe channel
	 */
	protected SinkChannel sinkChannel = null;
	/**
	 * 事件处理器
	 */
	protected IEventHandle eventHandle;
	/**
	 * 当前服务
	 */
	protected Server server;

	protected IOBuffer pipeBuf = new IOBuffer(1);
	
	public EventManager getEv() {
		return ev;
	}

	public SinkChannel getSinkChannel() {
		return sinkChannel;
	}

	public Server getServer() {
		return server;
	}
	
	public void run() {
		try {
			ev = new EventManager();
			ev.addEvent(sourceChannel, EventType.EV_READ | EventType.EV_PERSIST, new Worker.PipeHandle(), this, 0);
			ev.loop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class PipeHandle implements IEventHandle {
		public void onIOReady(SelectableChannel channel, int type, Object obj) {
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
				conn.connectionCreate();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
