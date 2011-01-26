package xnet.mysqlproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.connection.ProxyHandle;
import xnet.core.event.*;
import xnet.core.io.IOState;
import xnet.core.io.IOUtil;
import xnet.mysqlproxy.state.*;

public class MysqlProxyHandle extends ProxyHandle {
	public static String host = "127.0.0.1";
	public static int port = 3306;

	protected static Log logger = LogFactory.getLog(MysqlProxyHandle.class);
	public static int BUFFER_SIZE = 1024;

	MysqlStateBase state;
	HandleState handleState;

	MysqlIOBuffer sReadBuffer;
	MysqlIOBuffer cReadBuffer;
	MysqlIOBuffer sWriteBuffer;
	MysqlIOBuffer cWriteBuffer;

	public MysqlIOBuffer getSReadBuffer() {
		return sReadBuffer;
	}

	public MysqlIOBuffer getCReadBuffer() {
		return cReadBuffer;
	}

	public MysqlIOBuffer getSWriteBuffer() {
		return sWriteBuffer;
	}

	public MysqlIOBuffer getCWriteBuffer() {
		return cWriteBuffer;
	}

	public MysqlProxyHandle() {
		sReadBuffer = new MysqlIOBuffer();
		cReadBuffer = new MysqlIOBuffer();
		sWriteBuffer = new MysqlIOBuffer();
		cWriteBuffer = new MysqlIOBuffer();
		state = new MysqlStateRSInit(this, 1);
	}

	/**
	 * connect to mysql server
	 * 
	 * @throws Exception
	 */
	protected void connectServer() throws Exception {
		sSocket = SocketChannel.open();
		sSocket.connect(new InetSocketAddress(host, port));
		sSocket.configureBlocking(false);
	}

	/**
	 * 同时注册client和server的读事件，用于竞争读
	 * 
	 * @throws Exception
	 */
	protected void selectIO() throws Exception {
		sReadBuffer.clear();
		sReadBuffer.limit(BUFFER_SIZE);
		worker.addEvent(sSocket, EventType.EV_READ, this, cReadTimeout);

		cReadBuffer.clear();
		cReadBuffer.limit(BUFFER_SIZE);
		worker.addEvent(cSocket, EventType.EV_READ, this, sReadTimeout);
	}

	/**
	 * 处理client或server的io事件
	 * 
	 * @param socket
	 * @param rtosocket
	 * @param type
	 * @param rbuf
	 * @param rtobuf
	 * @param wbuf
	 * @param rtimeout
	 * @param rtotimeout
	 * @param wtimeout
	 * @throws Exception
	 */
	void handle(SocketChannel socket, SocketChannel rtosocket, int type, MysqlIOBuffer rbuf, MysqlIOBuffer rtobuf, MysqlIOBuffer wbuf, long rtimeout, long rtotimeout, long wtimeout) throws Exception {
		IOState ioState = null;
		boolean stop = false;

		if ((type & EventType.EV_CONNECT) > 0) {
			// 新连接
			connectServer();
			worker.addEvent(sSocket, EventType.EV_READ, this, sReadTimeout);
			return;
		}

		do {
			switch (state.getState()) {
			case MysqlStateRSInit:
			case MysqlStateRSResult:
			case MysqlStateRSFields:
			case MysqlStateRSRows:
				socket = sSocket;
				rtimeout = sReadTimeout;
				wtimeout = sWriteTimeout;
				break;
			case MysqlStateWSAuth:
			case MysqlStateWSCommond:
				socket = sSocket;
				rtimeout = sReadTimeout;
				wtimeout = sWriteTimeout;
				break;
			case MysqlStateRCAuth:
			case MysqlStateRCCommond:
				socket = cSocket;
				rtimeout = cReadTimeout;
				wtimeout = cWriteTimeout;
				break;
			case MysqlStateWCInit:
			case MysqlStateWCResult:
			case MysqlStateWCFields:
			case MysqlStateWCRows:
				socket = cSocket;
				rtimeout = cReadTimeout;
				wtimeout = cWriteTimeout;
				break;
			}

			if (state.isRead()) {
				int newLimit = state.getRbuf().limit() + BUFFER_SIZE - state.getRbuf().remaining();
				state.getRbuf().limit(newLimit);
				ioState = IOUtil.nread(socket, state.getRbuf());
				// 将limit置为当前读到的pos，表示缓冲区当前有limit长度的数据
				state.getRbuf().limit(state.getRbuf().position());
			} else {
				ioState = IOUtil.nwrite(socket, state.getWbuf());
			}
			if (ioState == IOState.ERROR) {
				// IO出错
				close();
				stop = true;
				break;
			}

			if (ioState == IOState.CLOSE) {
				close();
				stop = true;
				logger.debug("finishe a connection");
				break;
			}

			MysqlStateBase nextState;
			if (state.isRead()) {
				// 切换状态,置读buf的pos为0
				state.getRbuf().position(0);
				nextState = state.next();
				if (nextState == null) {
					// packet未准备好，需要继续读
					state.getRbuf().position(state.getRbuf().limit());
					worker.addEvent(socket, EventType.EV_READ, this, rtimeout);
					stop = true;
					break;
				} else {
					// 下一状态必须为写
					state = nextState;
					state.getWbuf().position(0);
				}
			} else {
				if (ioState == IOState.GOON) {
					// IO继续,需要继续写
					worker.addEvent(socket, EventType.EV_WRITE, this, wtimeout);
					stop = true;
					break;
				}

				// 写完数据，切换状态,将写buf重置
				state.getWbuf().position(0);
				state.getWbuf().limit(0);
				nextState = state.next();
				// 下一状态必须为读
				state = nextState;
				// 重置rbuf，重用已经写出去的数据buffer
				int newPos = state.getRbuf().remaining();
				state.getRbuf().compact();
				state.getRbuf().position(newPos);
				state.getRbuf().limit(newPos);
				
				worker.addEvent(socket, EventType.EV_READ, this, rtimeout);
				stop = true;
				break;
			}
		} while (!stop);
	}

	@Override
	public void handleServer(SocketChannel socket, int type) throws Exception {
		logger.debug("handleServer.EventType:" + EventType.getString(type));
		MysqlIOBuffer rbuf = sReadBuffer;
		MysqlIOBuffer rtobuf = cWriteBuffer;
		MysqlIOBuffer wbuf = sWriteBuffer;
		long rtimeout = sReadTimeout;
		long rtotimeout = cWriteTimeout;
		long wtimeout = sWriteTimeout;
		SocketChannel rtosocket = cSocket;

		handle(socket, rtosocket, type, rbuf, rtobuf, wbuf, rtimeout, rtotimeout, wtimeout);
	}

	@Override
	public void handleClient(SocketChannel socket, int type) throws Exception {
		logger.debug("handleClient.EventType:" + EventType.getString(type));
		MysqlIOBuffer rbuf = cReadBuffer;
		MysqlIOBuffer rtobuf = sWriteBuffer;
		MysqlIOBuffer wbuf = cWriteBuffer;
		long rtimeout = cReadTimeout;
		long rtotimeout = sWriteTimeout;
		long wtimeout = cWriteTimeout;
		SocketChannel rtosocket = sSocket;

		handle(socket, rtosocket, type, rbuf, rtobuf, wbuf, rtimeout, rtotimeout, wtimeout);
	}

	enum HandleState {
		HANDLE_WRITE, HANDLE_READ
	}

}
