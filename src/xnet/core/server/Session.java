package xnet.core.server;

import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.util.IOBuffer;

/**
 * session，表示一次会话
 * 
 * @author quanwei
 * 
 */
public abstract class Session {
	static Log logger = LogFactory.getLog(Session.class);

	/**
	 * 当前的session 等待的IO状态 3种状态：读状态,写状态,关闭
	 */
	public static final int STATE_READ = 0;
	public static final int STATE_WRITE = 1;
	public static final int STATE_CLOSE = 2;

	/**
	 * 当前session的事件 3种事件：可读，可写，超时
	 */
	public static final int EVENT_READ = 0;
	public static final int EVENT_WRITE = 1;
	public static final int EVENT_TIMEOUT = 2;

	/**
	 * 下一次超时时间点（时间戳）
	 */
	long nextTimeout = 0;
	/**
	 * 当前状态，读OR写
	 */
	int state;
	/**
	 * 当前的事件
	 */
	int event = Session.EVENT_READ;
	/**
	 * 读buffer
	 */
	IOBuffer readBuf = null;
	/**
	 * 写buffer
	 */
	IOBuffer writeBuf = null;
	/**
	 * socket
	 */
	SocketChannel socket = null;
	/**
	 * 全局配置
	 */
	Config config = null;
	/**
	 * 正在被使用
	 */
	boolean inuse = false;

	/**
	 * 连接建立回调函数
	 * 
	 * @param readBuf
	 *            请求包
	 * @param writeBuf
	 *            响应包
	 * @throws Exception
	 */
	public abstract void open(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception;

	/**
	 * 所有数据读取完成
	 * 
	 * @param readBuf
	 *            请求包
	 * @param writeBuf
	 *            响应包
	 * @throws Exception
	 */
	public abstract void complateRead(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception;

	/**
	 * 本次数据读取完成
	 * 
	 * @param readBuf
	 *            请求包
	 * @param writeBuf
	 *            响应包
	 * @throws Exception
	 */
	public abstract void complateReadOnce(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception;

	/**
	 * 所有数据写入完成
	 * 
	 * @param readBuf
	 *            请求包
	 * @param writeBuf
	 *            响应包
	 * @throws Exception
	 */
	public abstract void complateWrite(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception;

	/**
	 * 本次数据写入完成
	 * 
	 * @param readBuf
	 *            请求包
	 * @param writeBuf
	 *            响应包
	 * @throws Exception
	 */
	public abstract void complateWriteOnce(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception;

	/**
	 * 连接关闭回调函数
	 * 
	 * @param in
	 *            请求包
	 * @param out
	 *            响应包
	 * @throws Exception
	 */
	public abstract void close();

	/**
	 * 超时处理
	 * 
	 * @throws Exception
	 */
	public abstract void timeout(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception;

	public void setNextState(int state) {
		this.state = state;
		if (state == STATE_WRITE) {
			readBuf.position(0);
			readBuf.limit(0);
		} else if (state == STATE_READ) {
			writeBuf.position(0);
			writeBuf.limit(0);
		}
	}

	public void remainToRead(int remain) {
		readBuf.limit(readBuf.position() + remain);
		setNextState(STATE_READ);
	}

	public void remainToWrite(int remain) {
		writeBuf.limit(writeBuf.position() + remain);
		setNextState(STATE_WRITE);
	}

}
