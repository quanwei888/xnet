# Xnet #
xnet是一个纯java实现的高性能的异步网络框架，同mina的区别是xnet是一个非常轻量级的框架，性能好于mina。
xnet基于网络事件触发回调函数的方式提供给开发者使用，使用xnet，只需专注业务逻辑的开发，支持高并发和高吞吐量，支持常用的网络服务配置，如读写超时设置、长短连接等配置。

# xnet提供以下的回调函数 #
```
/**
 * 连接建立回调函数
 * 
 * @param readBuf
 *            请求包
 * @param writeBuf
 *            响应包
 * @throws Exception
 */
public abstract void open(IOBuffer readBuf, IOBuffer writeBuf) throws Exception;

/**
 * 所有数据读取完成
 * 
 * @param readBuf
 *            请求包
 * @param writeBuf
 *            响应包
 * @throws Exception
 */
public abstract void complateRead(IOBuffer readBuf, IOBuffer writeBuf) throws Exception;

/**
 * 本次数据读取完成,默认不作处理
 * 
 * @param readBuf
 *            请求包
 * @param writeBuf
 *            响应包
 * @throws Exception
 */
public void complateReadOnce(IOBuffer readBuf, IOBuffer writeBuf) throws Exception;

/**
 * 所有数据写入完成
 * 
 * @param readBuf
 *            请求包
 * @param writeBuf
 *            响应包
 * @throws Exception
 */
public abstract void complateWrite(IOBuffer readBuf, IOBuffer writeBuf) throws Exception;

/**
 * 本次数据写入完成,默认不作处理
 * 
 * @param readBuf
 *            请求包
 * @param writeBuf
 *            响应包
 * @throws Exception
 */
public void complateWriteOnce(IOBuffer readBuf, IOBuffer writeBuf) throws Exception;

/**
 * 连接关闭后的回调函数,默认不作处理
 * 
 * @param in
 *            请求包
 * @param out
 *            响应包
 * @throws Exception
 */
public void close();
```

# 开发EchoServer #
1、创建一个session，用于处理xnet的网络回调事件
```
public class EchoSession extends Session {
	static Log logger = LogFactory.getLog(EchoSession.class);
	static final int BUF_SIZE = 1024;

	@Override
	public void complateRead(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception {
		logger.debug("DEBUG ENTER");
		complateReadOnce(readBuf, writeBuf);
	}

	@Override
	public void complateReadOnce(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception {
		logger.debug("DEBUG ENTER");
		if (readBuf.position() > 1) {
			byte b = readBuf.getByte(readBuf.position() - 1);
			if (b == (byte) '\n') {
				int len = readBuf.position();
				writeBuf.position(0);
				writeBuf.writeBytes(readBuf.readBytes(0, len));

				writeBuf.position(0);
				writeBuf.limit(len);
				setNextState(STATE_WRITE);
				return;
			}
		}
		remainToRead(BUF_SIZE);
	}

	@Override
	public void complateWrite(IOBuffer readBuf, IOBuffer writeBuf)
			throws Exception {
		logger.debug("DEBUG ENTER");
		readBuf.position(0);
		remainToRead(BUF_SIZE);
	}

	@Override
	public void open(IOBuffer readBuf, IOBuffer writeBuf) throws Exception {
		logger.debug("DEBUG ENTER");
		remainToRead(BUF_SIZE);
	}

	@Override
	public void timeout(IOBuffer readBuf, IOBuffer writeBuf) throws Exception {
		logger.debug("DEBUG ENTER");
		setNextState(STATE_CLOSE);
	}

	@Override
	public void close() {
		logger.debug("DEBUG ENTER");
	}

}
```

2、设置server属性并启动server
```
public class EchoServer {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println(EchoServer.class + " PORT THREAD_NUM\n");
			return;
		}

		Config config = new Config();
		config.session = EchoSession.class;
		config.threadNum = Integer.parseInt(args[1]);
		config.port = Short.parseShort(args[0]);
		config.rTimeout = 3000;
		config.wTimeout = 3000;
		config.ip = "0.0.0.0";
		config.keepalive = true;
		config.maxConnection = 1000;
		Server server = new Server(config);
		server.run();
	}
}
```