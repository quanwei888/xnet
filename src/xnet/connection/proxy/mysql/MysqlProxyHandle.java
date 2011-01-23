package xnet.connection.proxy.mysql;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
import xnet.connection.proxy.ProxyHandle; 
import xnet.core.*;
import xnet.core.event.*;

public class MysqlProxyHandle extends ProxyHandle {
	public static String host = "127.0.0.1";
	public static int port = 3306;
	
	protected static Log logger = LogFactory.getLog(MysqlProxyHandle.class);
	static int BUFFER_SIZE = 8192;

	State state = null;
	HandleState handleState = HandleState.HANDLE_READ;
	 

	protected MysqlIOBuffer sReadBuffer;
	protected MysqlIOBuffer cReadBuffer;
	protected MysqlIOBuffer sWriteBuffer;
	protected MysqlIOBuffer cWriteBuffer;

	public MysqlProxyHandle() {
		sReadBuffer = new MysqlIOBuffer();
		cReadBuffer = new MysqlIOBuffer();
		sWriteBuffer = new MysqlIOBuffer();
		cWriteBuffer = new MysqlIOBuffer();
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
		addEvent(sSocket, EventType.EV_READ, cReadTimeout);

		cReadBuffer.clear();
		cReadBuffer.limit(BUFFER_SIZE);
		addEvent(cSocket, EventType.EV_READ, sReadTimeout);
	}

	/**
	 * 获取已读完的数据中最后一个完整包的结束位置
	 */
	int getLastPacketEndPos(MysqlIOBuffer buffer) {
		int pos = 0;
		int size = 0;

		while (pos + 4 <= buffer.limit()) {
			size = buffer.getBodyLen(pos);
			pos += 4 + size;
		}
		return pos > 0 && pos <= buffer.limit() ? pos : -1;
	}

	/**
	 * 处理client或server的io事件
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
	void handle(SocketChannel socket, SocketChannel rtosocket, int type,
			MysqlIOBuffer rbuf, MysqlIOBuffer rtobuf, MysqlIOBuffer wbuf,
			long rtimeout, long rtotimeout, long wtimeout) throws Exception {
		IOState ioState = null;
		boolean stop = true;

		do {
			stop = true;
			switch (type) {
			case EventType.EV_CONNECT:
				// 新链接建立
				connectServer();
				selectIO();
				break;
			case EventType.EV_READ:
				if (handleState == HandleState.HANDLE_WRITE) {
					// 当前在写，忽略读事件
					return;
				}

				// 从server读到sReadBuffer
				ioState = IOUtil.nread(socket, rbuf);

				// IO出错
				if (ioState == IOState.ERROR) {
					throw new IOException("IO ERROR");
				}

				// IO关闭
				if (ioState == IOState.CLOSE) {
					logger.debug("finished a connection");
					close();
					return;
				}

				rbuf.limit(rbuf.position());
				int endPos = getLastPacketEndPos(rbuf);
				if (endPos == -1) {
					// 还未读完一个包
					rbuf.limit(rbuf.limit() + BUFFER_SIZE);
					addEvent(socket, EventType.EV_READ, rtimeout);
					break;
				}

				// 至少读完了1个包,将已读完的包写出去
				int limit = endPos;
				// 读完整个packet,复制 sReadBuffer到cWriteBuffer，打开client的写事件
				byte[] bytes = new byte[limit];

				/* 拷贝sReadBuffer到cWriteBuffer */
				rbuf.position(0);
				rbuf.getBuf().get(bytes, 0, limit);
				// 准备写buffer
				rtobuf.position(0);
				rtobuf.limit(limit);
				rtobuf.getBuf().put(bytes);
				rtobuf.position(0);

				/* 将未写出去的数据往前移到 */
				rbuf.position(limit);
				rbuf.getBuf().compact();
				rbuf.limit(limit);
				rbuf.position(limit);
				rbuf.limit(limit + BUFFER_SIZE);
				logger.debug(rbuf.getBuf());

				// 切换到写
				addEvent(rtosocket, EventType.EV_WRITE, rtotimeout);
				handleState = HandleState.HANDLE_WRITE;
				break;
			case EventType.EV_WRITE:
				// 将sWriteBuffer内容写到server
				ioState = IOUtil.nwrite(socket, wbuf);
				// IO出错
				if (ioState == IOState.ERROR) {
					throw new Exception("IO ERROR");
				}

				// IO继续
				if (ioState == IOState.GOON) {
					addEvent(socket, EventType.EV_WRITE, wtimeout);
					break;
				}

				// IO完成
				selectIO();
				handleState = HandleState.HANDLE_READ;
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

		handle(socket, rtosocket, type, rbuf, rtobuf, wbuf, rtimeout,
				rtotimeout, wtimeout);
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

		handle(socket, rtosocket, type, rbuf, rtobuf, wbuf, rtimeout,
				rtotimeout, wtimeout);
	}

	enum HandleState {
		HANDLE_WRITE,
		HANDLE_READ
	}

	enum State {
		READ_SERVER_INIT,
		WRITE_CLIENT_INIT,

		READ_CLIENT_AUTH,
		WRITE_SERVER_AUTh,

		READ_SERVER_RESULT,
		WRITE_CLIENT_RESULT,

		READ_CLIENT_COMMOND,
		WRITE_SERVER_COMMOND,

		READ_SERVER_FIELDS,
		WRITE_CLIENT_FIELDS,

		READ_SERVER_DATA,
		WRITE_CLIENT_DATA,

		READ_CLINET_DATA,
		WRITE_SERVER_DATA

	}

}
