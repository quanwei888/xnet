package xnet.mysqlproxy.state;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.mysqlproxy.MysqlProxyHandle; 

/**
 * read init from server
 * 
 * @author quanwei
 * 
 */
public class MysqlStateWCResult extends MysqlStateBase {
	static Log logger = LogFactory.getLog(MysqlStateWCResult.class);

	public MysqlStateWCResult(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getcSocket();
		logger.debug("New State:" + MysqlStateWCResult.class);
	}

	@Override
	public MysqlStateBase getNext() {
		// Ð´Íêresult packet
		MysqlStateBase nextState;
		if (remain == 0) {
			nextState = new MysqlStateRCCommond(handle, 1);
		} else {
			nextState = new MysqlStateRSFields(handle, 1);
		}
		return nextState;
	}

	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateWCResult;
	}
}
