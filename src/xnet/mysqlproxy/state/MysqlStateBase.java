package xnet.mysqlproxy.state;

import java.nio.channels.SocketChannel;

import xnet.mysqlproxy.MysqlIOBuffer;
import xnet.mysqlproxy.MysqlProxyHandle;

public abstract class MysqlStateBase {
	protected SocketChannel socket;
	
	public SocketChannel getSocket() {
		return socket;
	}

	protected boolean isRead = false;
	public boolean isRead() {
		return isRead;
	}

	protected MysqlIOBuffer rbuf;

	public MysqlIOBuffer getRbuf() {
		return rbuf;
	}

	protected MysqlIOBuffer wbuf;

	public MysqlIOBuffer getWbuf() {
		return wbuf;
	}

	protected MysqlProxyHandle handle;
	protected long remain = 1;

	public MysqlStateBase(MysqlProxyHandle handle, long remain) {
		this.handle = handle;
		this.remain = remain;
	}

	public MysqlStateBase next() throws Exception {
		MysqlStateBase nextState = getNext();
		if (rbuf.hasNextPacket()) {
			nextState = nextState.next();
		}
		return nextState;
	}

	public abstract MysqlStateBase getNext();

	public abstract MysqlStateEnum getState();

}
