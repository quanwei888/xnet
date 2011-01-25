package xnet.core.connection;

import java.nio.channels.SelectableChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.event.*;
import xnet.core.io.IOBuffer;
import xnet.core.io.IOState;

/**
 * 简单的connection 实现了request->response的过程
 * 
 * @author quanwei
 * 
 */
public class SimpleConnection extends Connection implements IEventHandle {
	static Log logger = LogFactory.getLog(SimpleConnection.class);

	/**
	 * 当前状态
	 */
	protected IConnectionState state = new SimpleState();
	/**
	 * 读buffer
	 */
	protected IOBuffer readBuf = new IOBuffer();
	/**
	 * 写buffer
	 */
	protected IOBuffer writeBuf = new IOBuffer();
	/**
	 * 请求处理器
	 */
	protected ISimpleHandle handle;

	public void setHandle(ISimpleHandle handle) {
		this.handle = handle;
	}

	IOState read() {
		return nwrite(socketChannel, readBuf);
	}

	IOState write() {
		return nwrite(socketChannel, writeBuf);
	}

	protected void initState() throws Exception {
		state.setValue(SimpleState.READ_REQ);
		handle.sessionOpen(readBuf, writeBuf);
		long timeout = worker.getServer().getReadTimeout();
		worker.getEv().addEvent(socketChannel, EventType.EV_READ | EventType.EV_PERSIST, this, null, timeout);
	}

	public void execute() {
		try {
			handle.sessionCreate(readBuf, writeBuf);
			initState();
		} catch (Exception e) {
			state.setValue(SimpleState.CLOSE);
			logger.warn(e.getMessage());
		}
	}

	/**
	 * 异步的io事件处理
	 */
	public void onIOEvent(SelectableChannel select, int type, Object obj) {
		boolean stop = false;
		IOState ioState = IOState.ERROR;
		logger.debug("connection event start:" + type);
		if ((type & EventType.EV_TIMEOUT) > 0) {
			// 超时
			logger.warn("connection timeout");
			stop = true;
		}
		do {
			ioState = IOState.ERROR;
			logger.debug("state:" + state.getValue());
			switch (state.getValue()) {
			case SimpleState.READ_REQ:
				ioState = read();
				logger.debug("iostate:" + ioState);
				if (ioState == IOState.ERROR) {
					state.setValue(SimpleState.CLOSE);
					break;
				}
				int newLimit = handle.remain(readBuf);
				if (newLimit > 0) {
					// 继续读
					readBuf.limit(newLimit);
					if (ioState == IOState.GOON) {
						stop = true;
						break;
					}
				} else if (newLimit == 0) {
					// 已接收完请求包，执行命令，并进入响应阶段
					state.setValue(SimpleState.WRITE_RES);
					try {
						// 处理请求
						handle.sessionHandle(readBuf, writeBuf);
						writeBuf.position(0);
						// 注册新事件
						long timeout = worker.getServer().getReadTimeout();
						worker.getEv().addEvent(socketChannel, EventType.EV_WRITE | EventType.EV_PERSIST, this, null, timeout);
					} catch (Exception e) {
						e.printStackTrace();
						state.setValue(SimpleState.CLOSE);
						break;
					}
				}
				break;
			case SimpleState.WRITE_RES:
				ioState = write();
				logger.debug("iostate:" + ioState);
				if (ioState == IOState.ERROR) {
					state.setValue(SimpleState.CLOSE);
				} else if (ioState == IOState.GOON) {
					stop = true;
				} else {
					try {
						handle.sessionClose(readBuf, writeBuf);
						if (server.isKeepalive()) {
							initState();
						} else {
							state.setValue(SimpleState.CLOSE);
						}
					} catch (Exception e) {
						state.setValue(SimpleState.CLOSE);
						logger.warn(e.getMessage());
					}
				}
				break;
			case SimpleState.CLOSE:
			default:
				// 关闭连接
				logger.debug("close connection");
				try {
					// 删除原来注册的事件
					worker.getEv().delEvent(socketChannel);
					handle.sessionDestrory(readBuf, writeBuf);
					socketChannel.socket().close();
					ConnectionPool.free(this);
				} catch (Exception e) {
					logger.warn(e.getMessage());
				}
				stop = true;
				break;
			}
		} while (!stop);
	}

	class SimpleState extends ConnectionState {
		public final static int READ_REQ = 1;
		public final static int WRITE_RES = 2;
		public final static int CLOSE = 3;
	}
}
