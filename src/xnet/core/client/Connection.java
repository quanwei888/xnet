package xnet.core.client;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.util.IOBuffer;
import xnet.core.util.NetIo;

/**
 * client连接对象
 * 
 * @author quanwei
 * 
 */
public class Connection {
	static Log logger = LogFactory.getLog(Connection.class);
	/**
	 * 连接超时（ms）
	 */
	public int cTimeout = 0;
	/**
	 * 读超时（ms）
	 */
	public int rTimeout = 0;
	/**
	 * 写超时（ms）
	 */
	public int wTimeout = 0; 

	/**
	 * IP
	 */
	public List<InetSocketAddress> servers;
	Selector selector;
	SocketChannel socket;

	public Connection(List<InetSocketAddress> servers, int cTimeout,
			int rTimeout, int wTimeout) {
		this.cTimeout = cTimeout;
		this.rTimeout = rTimeout;
		this.wTimeout = wTimeout;
		this.servers = servers;
	}

	public void connect() throws Exception {
		if (servers.size() == 0) {
			throw new Exception("epmty servers");
		}
		selector = Selector.open();
		Random r = new Random();
		int start = r.nextInt(servers.size());
		int index = start;
		while (true) {
			socket = connect(servers.get(index));
			if (socket != null) {
				break;
			}
			index++;
			index = index % servers.size();
			if (index == start) {
				break;
			}
		}
		if (socket == null) {
			throw new Exception("connect all servers error");
		}
	}

	/**
	 * 连接一台server
	 * 
	 * @param server
	 * @return
	 */
	SocketChannel connect(InetSocketAddress server) {
		SocketChannel socket = null;
		try {
			Selector selector = Selector.open();
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			socket.connect(server);
			socket.register(selector, SelectionKey.OP_CONNECT);
			selector.select(cTimeout);
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			if (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isConnectable() && socket.isConnectionPending()) {
					socket.finishConnect();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (socket != null && socket.keyFor(selector) != null) {
			socket.keyFor(selector).cancel();
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (socket != null && !socket.isConnected()) {
			// 超时或连接失败
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		return socket;
	}

	/**
	 * write数据
	 * 
	 * @param stream
	 * @throws Exception
	 */
	public void write(byte[] stream) throws Exception {
		IOBuffer writeBuf = new IOBuffer();
		writeBuf.position(0);
		writeBuf.limit(stream.length);
		writeBuf.writeBytes(stream);
		writeBuf.position(0);
		write(writeBuf);
	}

	/**
	 * write数据
	 * 
	 * @param stream
	 * @throws Exception
	 */
	public void write(IOBuffer writeBuf) throws Exception {

		try {
			Selector selector = Selector.open();
			socket.register(selector, SelectionKey.OP_WRITE);

			int remainTime = wTimeout;
			while (remainTime >= 0) {
				long stime = System.currentTimeMillis();
				int ret = selector.select(remainTime);

				if (ret == 0) {
					// 超时
					logger.warn("write time out");
					break;
				}

				NetIo.write(socket, writeBuf);
				if (writeBuf.remaining() == 0) {
					break;
				}
				remainTime -= System.currentTimeMillis() - stime;
				if (remainTime <= 0) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (socket != null && socket.keyFor(selector) != null) {
			socket.keyFor(selector).cancel();
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (writeBuf.remaining() > 0) {
			close();
			throw new Exception("write error");
		}
	}

	/**
	 * read数据
	 * 
	 * @param len
	 * @return
	 * @throws Exception
	 */
	public byte[] read(int len) throws Exception {
		IOBuffer readBuf = new IOBuffer();
		readBuf.position(0);
		readBuf.limit(len);
		read(readBuf);
		return readBuf.getBytes(0, len);
	}

	/**
	 * read数据
	 * 
	 * @param len
	 * @return
	 * @throws Exception
	 */
	public void read(IOBuffer readBuf) throws Exception {
		try {
			Selector selector = Selector.open();
			socket.register(selector, SelectionKey.OP_READ);

			int remainTime = rTimeout;
			while (remainTime >= 0) {
				long stime = System.currentTimeMillis();
				int ret = selector.select(remainTime);

				if (ret == 0) {
					// 超时
					logger.warn("read time out");
					break;
				}
				NetIo.read(socket, readBuf);
				if (readBuf.remaining() == 0) {
					break;
				}
				remainTime -= System.currentTimeMillis() - stime;
				if (remainTime <= 0) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (socket != null && socket.keyFor(selector) != null) {
			socket.keyFor(selector).cancel();
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (readBuf.remaining() > 0) {
			close();
			throw new Exception("read error");
		}
	}

	/**
	 * 关闭连接
	 */
	public void close() {
		if (socket == null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

}
