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
public class MysqlStateWCFields extends MysqlStateBase {
	static Log logger = LogFactory.getLog(MysqlStateWCFields.class);

	public MysqlStateWCFields(MysqlProxyHandle handle, long remain) {
		super(handle, remain);
		this.rbuf = handle.getSReadBuffer();
		this.wbuf = handle.getCWriteBuffer();
		this.socket = handle.getcSocket();
		logger.debug("New State:" + MysqlStateWCFields.class);
	}

	@Override
	public MysqlStateBase getNext() {
		if (remain == 0) {
			return new MysqlStateRSRows(handle, 1);
		} else {
			return new MysqlStateRSFields(handle, remain);
		}
	}
	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateWCFields;
	}
}
