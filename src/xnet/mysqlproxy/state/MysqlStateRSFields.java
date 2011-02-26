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
public class MysqlStateRSFields extends MysqlStateRead {
	static Log logger = LogFactory.getLog(MysqlStateRSFields.class);

	public MysqlStateRSFields(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getsSocket();
		logger.debug("New State:" + MysqlStateRSFields.class);
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
		return new MysqlStateWCFields(handle, remain);
	}

	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateRSFields;
	}

}
