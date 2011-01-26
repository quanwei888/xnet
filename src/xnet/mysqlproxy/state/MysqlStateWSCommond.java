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
public class MysqlStateWSCommond extends MysqlStateBase {
	static Log logger = LogFactory.getLog(MysqlStateWSCommond.class);

	public MysqlStateWSCommond(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getCReadBuffer();
		this.wbuf = handle.getSWriteBuffer();
		logger.debug("New State:" + MysqlStateWSCommond.class);
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateRSResult(handle,remain);
	}
	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateWSCommond;
	}
}
