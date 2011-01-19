package xnet.server;

import java.io.IOException;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectableChannel;

import xnet.connection.*;
import xnet.event.*;
import xnet.io.IOBuffer;


public class Worker implements Runnable {
	/**
	 * �¼�������
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
	 * �¼�������
	 */
	protected IEventHandle eventHandle;
	/**
	 * ��ǰ����
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
		public void handle(SelectableChannel channel, int type, Object obj) {
			SourceChannel sourceChannel = (SourceChannel) channel;
			try {
				// �ӹܵ���1byte
				pipeBuf.clear();
				sourceChannel.read(pipeBuf.getBuf());
				System.out.println("worker read from pipe");

				if (pipeBuf.remaining() != 0) {
					return;
				}

				// ��connection����ȡ��һ��������
				IConnection conn = ConnectionPool.popQueue();
				if (conn == null) {
					return;
				}
				conn.setWorker((Worker) obj);
				conn.setServer(server);
				// ���ӽ���
				conn.connectionCreate();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
