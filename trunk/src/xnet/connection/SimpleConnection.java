package xnet.connection;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;

import xnet.event.*;
import xnet.io.IOBuffer;

/**
 * 简单的connection 实现了request->response的过程
 * 
 * @author quanwei
 * 
 */
public class SimpleConnection extends Connection implements IEventHandle {
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

	/**
	 * 从网络读buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	IoState read() {
		try {
			int len = socketChannel.read(readBuf.getBuf());
			System.out.println("remdin:" + readBuf.remaining() + ",read len:" + len);
			if (len <= 0) {
				return IoState.ERROR;
			}
			if (readBuf.remaining() == 0) {
				return IoState.COMPLATE;
			} else {
				return IoState.GOON;
			}
		} catch (IOException e) {
			return IoState.ERROR;
		}
	}

	/**
	 * 从网络写buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	IoState write() {
		try {
			int len = socketChannel.write(writeBuf.getBuf());	
			System.out.println("remdin:" + writeBuf.remaining() + ",write len:" + len);
			if (len <= 0) {
				return IoState.ERROR;
			}
			if (writeBuf.remaining() == 0) {
				return IoState.COMPLATE;
			} else {
				return IoState.GOON;
			}
		} catch (IOException e) {
			return IoState.ERROR;
		}
	}

	protected void initState() {
		state.setValue(SimpleState.READ_REQ);
		// 初始化readbuf
		readBuf.clear();
		readBuf.limit(handle.remain(readBuf));

		// 初始化writebuf
		writeBuf.clear();
		writeBuf.limit(0);

		// 读事件
		try {
			worker.getEv().addEvent(socketChannel,
					EventType.EV_READ | EventType.EV_PERSIST, this, null, 0);
		} catch (ClosedChannelException e) {
			state.setValue(SimpleState.CLOSE);
		}
	}

	public void connectionCreate() {
		initState();
	}

	public void connectionClose() {

	}

	public void requestHandle() {

	}

	/**
	 * 异步的io事件处理
	 */
	public void handle(SelectableChannel select, int type, Object obj) {
		boolean stop = false;
		IoState ioState = IoState.ERROR;
		System.out.println("connection event start:" + type);
		do {
			stop = false;
			ioState = IoState.ERROR;
			System.out.println("state:" + state.getValue());
			switch (state.getValue()) {
			case SimpleState.READ_REQ:
				ioState = read();
				System.out.println("iostate:" + ioState);
				if (ioState == IoState.ERROR) {
					state.setValue(SimpleState.CLOSE);
					break;
				}
				int newLimit = handle.remain(readBuf);
				if (newLimit > 0) {
					// 继续读
					readBuf.limit(newLimit);
					if (ioState == IoState.GOON) {
						stop = true;
						break;
					}
				} else if (newLimit == 0) {
					// 已接收完请求包，执行命令，并进入响应阶段
					state.setValue(SimpleState.WRITE_RES);
					try {
						// 处理请求
						handle.handle(readBuf, writeBuf);
						writeBuf.position(0);
						// 注册新事件
						worker.getEv().addEvent(socketChannel,
								EventType.EV_WRITE | EventType.EV_PERSIST,
								this, null, 0);
					} catch (Exception e) {
						e.printStackTrace();
						state.setValue(SimpleState.CLOSE);
						break;
					}
				}
				break;
			case SimpleState.WRITE_RES:
				ioState = write();
				System.out.println("iostate:" + ioState);
				if (ioState == IoState.ERROR) {
					state.setValue(SimpleState.CLOSE);
				} else if (ioState == IoState.GOON) {
					stop = true;
				} else {
					if (server.isKeepalive()) {
						initState();
					} else {
						state.setValue(SimpleState.CLOSE);
					}
				}
				break;
			case SimpleState.CLOSE:
			default:
				// 关闭连接
				System.out.println("close connection");
				try {
					// 删除原来注册的事件
					worker.getEv().delEvent(socketChannel);
					socketChannel.socket().close();
					ConnectionPool.free(this);
				} catch (IOException e) {
					e.printStackTrace();
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

	enum IoState {
		GOON, COMPLATE, ERROR
	}
}
