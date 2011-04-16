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
	 * 当前的session 等待的IO状态 2种状态：读状态和写状态
	 */
	public static final int STATE_READ = 0;
	public static final int STATE_WRITE = 1;

	/**
	 * 当前session的事件 3种事件：可读，可写，超时
	 */
	public static final int EVENT_READ = 0;
	public static final int EVENT_WRITE = 1;
	public static final int EVENT_TIMEOUT = 2;

	/**
	 * 下一次超时时间点（时间戳）
	 */
	public long nextTimeout = 0;
	/**
	 * 当前状态，读OR写
	 */
	public int state;
	/**
	 * 当前的事件
	 */
	public int event = Session.EVENT_READ;
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
	 * 从client第一次读取的字节数
	 * @return
	 */
	public abstract int getInitReadBuf();
	
	/**
	 * 连接建立回调函数
	 * 
	 * @param in
	 *            请求包
	 * @param out
	 *            响应包
	 * @throws Exception
	 */
	public abstract void open() throws Exception;

	/**
	 * 还剩多少字节要读 完成一次IO时调用
	 * 
	 * @param buf
	 * @return 剩余的字节数
	 * @throws Exception
	 */
	public abstract int remain(IOBuffer readBuf) throws Exception;

	/**
	 * 接受完请求后的处理回调函数 完成请求的处理，并填充响应包
	 * 
	 * @param in
	 *            请求包
	 * @param out
	 *            响应包
	 * @throws Exception
	 */
	public abstract void handle(IOBuffer readBuf, IOBuffer writeBuf) throws Exception;

	/**
	 * 连接关闭回调函数
	 * 
	 * @param in
	 *            请求包
	 * @param out
	 *            响应包
	 * @throws Exception
	 */
	public abstract void close() throws Exception;

	/**
	 * 超时处理
	 * 
	 * @throws Exception
	 */
	public abstract void timeout() throws Exception;

}
