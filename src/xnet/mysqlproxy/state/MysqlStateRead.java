package xnet.mysqlproxy.state;

import xnet.mysqlproxy.MysqlProxyHandle;

public abstract class MysqlStateRead extends MysqlStateBase {
	public MysqlStateRead(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		isRead = true;
	}

	public MysqlStateBase next() throws Exception {
		if (!rbuf.hasNextPacket()) {
			return null;
		}
		
		byte[] bytes = rbuf.readNextPacket();
		wbuf.writeBytes(bytes);
		MysqlStateBase nextState = getNext();
		if (rbuf.hasNextPacket()) {
			nextState = nextState.next();
		}
		return nextState;
	}

}
