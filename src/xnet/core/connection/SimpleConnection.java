package xnet.core.connection;

import java.io.IOException; 
import java.nio.channels.SelectableChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

import xnet.core.Connection;
import xnet.core.ConnectionPool;
import xnet.core.IOBuffer;
import xnet.core.event.*; 

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

	/**
	 * �������buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	IoState read() {
		try {
			int len = socketChannel.read(readBuf.getBuf());
			logger.debug("remdin:" + readBuf.remaining() + ",read len:"
					+ len);
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
	 * ������дbuffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	IoState write() {
		try {
			int len = socketChannel.write(writeBuf.getBuf());
			logger.debug("remdin:" + writeBuf.remaining() + ",write len:"
					+ len);
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
		try {
			handle.beginRequest(readBuf, writeBuf);
			worker.getEv().addEvent(socketChannel,
					EventType.EV_READ | EventType.EV_PERSIST, this, null, 0);
		} catch (Exception e) {
			state.setValue(SimpleState.CLOSE);
		}
	}

	public void connectionCreate() {
		try {
			initState();
		} catch (Exception e) {
			state.setValue(SimpleState.CLOSE);
		}
	}

	public void connectionClose() {

	}

	public void requestHandle() {

	}

	/**
	 * �첽��io�¼�����
	 */
	public void onIOReady(SelectableChannel select, int type, Object obj) {
		boolean stop = false;
		IoState ioState = IoState.ERROR;
		logger.debug("connection event start:" + type);
		do {
			stop = false;
			ioState = IoState.ERROR;
			logger.debug("state:" + state.getValue());
			switch (state.getValue()) {
			case SimpleState.READ_REQ:
				ioState = read();
				logger.debug("iostate:" + ioState);
				if (ioState == IoState.ERROR) {
					state.setValue(SimpleState.CLOSE);
					break;
				}
				int newLimit = handle.remain(readBuf);
				if (newLimit > 0) {
					// ������
					readBuf.limit(newLimit);
					if (ioState == IoState.GOON) {
						stop = true;
						break;
					}
				} else if (newLimit == 0) {
					// �ѽ������������ִ�������������Ӧ�׶�
					state.setValue(SimpleState.WRITE_RES);
					try {
						// ��������
						handle.doRequest(readBuf, writeBuf);
						writeBuf.position(0);
						// ע�����¼�
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
				logger.debug("iostate:" + ioState);
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
				// �ر�����
				logger.debug("close connection");
				try {
					// ɾ��ԭ��ע����¼�
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
