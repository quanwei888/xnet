package xnet.mysqlproxy.state;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.mysqlproxy.MysqlIOBuffer;
import xnet.mysqlproxy.MysqlProxyHandle;

/**
 * read init from server
 * 
 * @author quanwei
 * 
 */
public class MysqlStateWSAuth extends MysqlStateBase {
	static Log logger = LogFactory.getLog(MysqlStateWSAuth.class);

	protected MysqlStateWSAuth(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getCReadBuffer();
		this.wbuf = handle.getSWriteBuffer();
		logger.debug("New State:" + MysqlStateWSAuth.class);
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateRSResult(handle,1);
	}
	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateWSAuth;
	}
}
