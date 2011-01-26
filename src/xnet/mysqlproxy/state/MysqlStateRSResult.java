package xnet.mysqlproxy.state;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.mysqlproxy.MysqlIOBuffer;
import xnet.mysqlproxy.MysqlProxyHandle;
import xnet.mysqlproxy.PacketResult;
import xnet.mysqlproxy.PacketResultData;

/**
 * read init from server
 * 
 * @author quanwei
 * 
 */
public class MysqlStateRSResult extends MysqlStateRead {
	static Log logger = LogFactory.getLog(MysqlStateRSResult.class);

	public MysqlStateRSResult(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getsSocket();
		logger.debug("New State:" + MysqlStateRSResult.class);
	}

	public MysqlStateBase next() throws Exception {
		if (!rbuf.hasNextPacket()) {
			return null;
		}
		
		PacketResult result = rbuf.getResultPacket();
		byte[] bytes = rbuf.readNextPacket();
		wbuf.writeBytes(bytes);
		logger.debug(result);
		if (result instanceof PacketResultData) {
			remain = 1;
		} else {
			remain = 0;
		}
		MysqlStateBase nextState = getNext();
		if (rbuf.hasNextPacket()) {
			nextState = nextState.next();
		}
		return nextState;
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateWCResult(handle, remain);
	}

	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateRSResult;
	}
}
