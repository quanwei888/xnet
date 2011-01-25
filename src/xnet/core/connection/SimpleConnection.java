package xnet.core.connection;

import java.nio.channels.SelectableChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.event.*;
import xnet.core.io.IOBuffer;
import xnet.core.io.IOState;

/**
 * �򵥵�connection ʵ����request->response�Ĺ���
 * 
 * @author quanwei
 * 
 */
public class SimpleConnection extends Connection implements IEventHandle {
	static Log logger = LogFactory.getLog(SimpleConnection.class);

	/**
	 * ��ǰ״̬
	 */
	protected IConnectionState state = new SimpleState();
	/**
	 * ��buffer
	 */
	protected IOBuffer readBuf = new IOBuffer();
	/**
	 * дbuffer
	 */
	protected IOBuffer writeBuf = new IOBuffer();
	/**
	 * ��������
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
	 * �첽��io�¼�����
	 */
	public void onIOEvent(SelectableChannel select, int type, Object obj) {
		boolean stop = false;
		IOState ioState = IOState.ERROR;
		logger.debug("connection event start:" + type);
		if ((type & EventType.EV_TIMEOUT) > 0) {
			// ��ʱ
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
					// ������
					readBuf.limit(newLimit);
					if (ioState == IOState.GOON) {
						stop = true;
						break;
					}
				} else if (newLimit == 0) {
					// �ѽ������������ִ�������������Ӧ�׶�
					state.setValue(SimpleState.WRITE_RES);
					try {
						// ��������
						handle.sessionHandle(readBuf, writeBuf);
						writeBuf.position(0);
						// ע�����¼�
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
				// �ر�����
				logger.debug("close connection");
				try {
					// ɾ��ԭ��ע����¼�
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
