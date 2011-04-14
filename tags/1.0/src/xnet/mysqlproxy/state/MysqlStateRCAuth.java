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
public class MysqlStateRCAuth extends MysqlStateRead {
	static Log logger = LogFactory.getLog(MysqlStateRCAuth.class);

	protected MysqlStateRCAuth(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getCReadBuffer();
		this.wbuf = handle.getSWriteBuffer();
		this.socket = handle.getcSocket();
		logger.debug("New State:" + MysqlStateRCAuth.class);
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateWSAuth(handle, remain);
	}

	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateRCAuth;
	}
}
