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
public class MysqlStateWCInit extends MysqlStateBase {
	static Log logger = LogFactory.getLog(MysqlStateWCInit.class);

	public MysqlStateWCInit(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getcSocket();
		logger.debug("New State:" + MysqlStateWCInit.class);
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateRCAuth(handle, 1);
	}
	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateWCInit;
	}
}
