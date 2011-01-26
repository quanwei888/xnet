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
public class MysqlStateRSInit extends MysqlStateRead {
	static Log logger = LogFactory.getLog(MysqlStateRSInit.class);

	public MysqlStateRSInit(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getsSocket();

		logger.debug("New State:" + MysqlStateRSInit.class);
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateWCInit(handle, remain);
	}

	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateRSInit;
	}
}
