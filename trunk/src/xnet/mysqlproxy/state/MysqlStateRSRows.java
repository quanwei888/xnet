package xnet.mysqlproxy.state;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
import xnet.mysqlproxy.MysqlProxyHandle;
import xnet.mysqlproxy.PacketResult;
import xnet.mysqlproxy.PacketResultEof;

/**
 * read init from server
 * 
 * @author quanwei
 * 
 */
public class MysqlStateRSRows extends MysqlStateRead {
	static Log logger = LogFactory.getLog(MysqlStateRSRows.class);

	public MysqlStateRSRows(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getsSocket();
		logger.debug("New State:" + MysqlStateRSRows.class);
	}

	public MysqlStateBase next() throws Exception {
		if (!rbuf.hasNextPacket()) {
			return null;
		}
		remain = 1;
		while (rbuf.hasNextPacket()) {
			PacketResult result = rbuf.getResultPacket();
			byte[] bytes = rbuf.readNextPacket();
			wbuf.writeBytes(bytes);
			if (result instanceof PacketResultEof) {
				remain = 0;
				break;
			}
		}
		MysqlStateBase nextState = getNext();
		if (rbuf.hasNextPacket()) {
			nextState = nextState.next();
		}
		return nextState;
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateWCRows(handle, remain);
	}
	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateRSRows;
	}

}
