package xnet.mysqlproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.*;
import xnet.core.connection.ProxyHandle;
import xnet.core.event.*;
import xnet.core.io.IOState;
import xnet.core.io.IOUtil;

public class MysqlProxyHandle extends ProxyHandle {
	public static String host = "127.0.0.1";
	public static int port = 3306;

	protected static Log logger = LogFactory.getLog(MysqlProxyHandle.class);
	static int BUFFER_SIZE = 8192;

	MysqlState mysqlState;
	IODirection direction;
	HandleState handleState;

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
	public static int getLastPacketEndPos(MysqlIOBuffer buffer) {
		int pos = 0;
		int size = 0;

		while (pos + 4 <= buffer.limit()) {
			size = buffer.getBodyLen(pos);
			pos += 4 + size;
		}
		return pos > 0 && pos <= buffer.limit() ? pos : -1;
	}

	/**
	 * 获取已读完的数据中最后一个完整包的起始位置
	 */
	public static int getLastPacketStartPos(MysqlIOBuffer buffer) {
		int pos = 0;
		int size = 0;
		int spos = pos;

		while (pos + 4 <= buffer.limit()) {
			size = buffer.getBodyLen(pos);
			spos = pos;
			pos += 4 + size;
		}
		return pos > 0 && pos <= buffer.limit() ? spos : -1;
	}

	/**
	 * 获取一共读完多少个packet
	 * 
	 * @param buffer
	 * @return
	 */
	public static int getPacketCount(MysqlIOBuffer buffer) {
		int pos = 0;
		int size = 0;
		int count = -1;

		while (pos + 4 <= buffer.limit()) {
			size = buffer.getBodyLen(pos);
			pos += 4 + size;
			count++;
		}
		return pos > 0 && pos <= buffer.limit() ? count : -1;
	}

	String getByteString(byte[] bytes) {
		String hex = "";
		for(byte b : bytes) {
			hex += String.format("%X,", b);
		}
		return hex;
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
		boolean stop = true;

		logger.debug("current state:" + handleState);

		do {
			stop = true;
			switch (type) {
			case EventType.EV_CONNECT:
				// 新链接建立
				connectServer();
				selectIO();
				direction = IODirection.READ_SERVER;
				handleState = HandleState.HANDLE_READ;
				mysqlState = new MysqlStateRSInit();
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

				logger.debug(getByteString(bytes));
				/* 将未写出去的数据往前移到 */
				rbuf.position(limit);
				rbuf.getBuf().compact();
				rbuf.limit(limit);
				rbuf.position(limit);
				rbuf.limit(limit + BUFFER_SIZE);
				logger.debug(rbuf.getBuf());

				// 读完至少一个packet完成，切换到写
				addEvent(rtosocket, EventType.EV_WRITE, rtotimeout);
				handleState = HandleState.HANDLE_WRITE;
				mysqlState = mysqlState.switchIO(rtosocket == cSocket ? IODirection.WRITE_CLIENT : IODirection.WRITE_SERVER);
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

				// 写操作完成,切换到读
				selectIO();
				handleState = HandleState.HANDLE_READ;
				mysqlState = mysqlState.switchIO(rtosocket == cSocket ? IODirection.READ_CLIENT : IODirection.READ_SERVER);
				break;
			}
		} while (!stop);
		logger.debug("current state:" + handleState);
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

	interface MysqlState {
		public MysqlState switchIO(IODirection nextDirection) throws Exception;
	}

	/**
	 * read init from server
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateRSInit implements MysqlState {
		public MysqlStateRSInit() {
			logger.debug("New State:" + MysqlStateRSInit.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateWCInit();
		}
	}

	/**
	 * write init to client
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateWCInit implements MysqlState {
		public MysqlStateWCInit() {
			logger.debug("New State:" + MysqlStateWCInit.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateRCAuth();
		}
	}

	/**
	 * read auth from client
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateRCAuth implements MysqlState {
		public MysqlStateRCAuth() {
			logger.debug("New State:" + MysqlStateRCAuth.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateWSAuth();
		}
	}

	/**
	 * write auth to server
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateWSAuth implements MysqlState {
		public MysqlStateWSAuth() {
			logger.debug("New State:" + MysqlStateWSAuth.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateRSResult();
		}
	}

	/**
	 * read result from server
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateRSResult implements MysqlState {
		public MysqlStateRSResult() {
			logger.debug("New State:" + MysqlStateRSResult.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateWCResult();
		}
	}

	/**
	 * write result to client
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateWCResult implements MysqlState {
		public MysqlStateWCResult() {
			logger.debug("New State:" + MysqlStateWCResult.class);
		}

		public MysqlState switchIO(IODirection nextDirection) throws Exception {
			cWriteBuffer.position(0);
			PacketResult result = cWriteBuffer.readResultPacket();
			if (result instanceof PacketResultData) {
				return new MysqlStateRSData(((PacketResultData) result).fieldCount);
			} else {
				return new MysqlStateRCCommond();
			}
		}
	}

	/**
	 * read commond from client
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateRCCommond implements MysqlState {
		public MysqlStateRCCommond() {
			logger.debug("New State:" + MysqlStateRCCommond.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateWSCommond();
		}
	}

	/**
	 * write commond to server
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateWSCommond implements MysqlState {
		public MysqlStateWSCommond() {
			logger.debug("New State:" + MysqlStateWSCommond.class);
		}

		public MysqlState switchIO(IODirection nextDirection) {
			sWriteBuffer.position(0);
			PacketCommond commond = sWriteBuffer.readPacketCommond();
			logger.debug("SQL:" + commond.cmd);
			return new MysqlStateRSResult();
		}
	}

	/**
	 * read field data from server
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateRSData implements MysqlState {
		long fieldCount;
		long fieldRemain;

		public MysqlStateRSData(long fieldCount) throws Exception {
			logger.debug("New State:" + MysqlStateRSData.class);
			this.fieldCount = fieldCount + 1;
			this.fieldRemain = this.fieldCount;

			cWriteBuffer.position(0);
			int count = MysqlProxyHandle.getPacketCount(cWriteBuffer) - 1;
			if (count == -1) {
				throw new Exception("getPacketCount error");
			}
			fieldRemain -= count;
		}

		public MysqlState switchIO(IODirection nextDirection) {
			return new MysqlStateWCData(this);
		}

		public boolean isEnd() throws Exception {
			if (fieldRemain > 0) {
				cWriteBuffer.position(0);
				int count = MysqlProxyHandle.getPacketCount(cWriteBuffer);
				if (count == -1) {
					throw new Exception("getPacketCount error");
				}
				fieldRemain -= count;
			}
			if (fieldRemain < 0) {
				cWriteBuffer.position(0);
				int pos = getLastPacketStartPos(cWriteBuffer);
				PacketResult result = cWriteBuffer.readResultPacket(pos);
				if (result instanceof PacketResultEof) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * write field data to client
	 * 
	 * @author quanwei
	 * 
	 */
	class MysqlStateWCData implements MysqlState {
		MysqlStateRSData mysqlState;

		public MysqlStateWCData(MysqlStateRSData mysqlState) {
			this.mysqlState = mysqlState;
			logger.debug("New State:" + MysqlStateWCData.class);
		}

		public MysqlState switchIO(IODirection nextDirection) throws Exception {
			if (mysqlState.isEnd()) {
				return new MysqlStateRCCommond();
			} else {
				logger.debug("New State:" + MysqlStateRSData.class);
				return mysqlState;
			}
		}
	}

	enum HandleState {
		HANDLE_WRITE, HANDLE_READ
	}

	enum IODirection {
		READ_CLIENT, READ_SERVER, WRITE_CLIENT, WRITE_SERVER,
	}
}
