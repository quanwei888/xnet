package xnet.mysqlproxy.state;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.mysqlproxy.MysqlIOBuffer;
import xnet.mysqlproxy.MysqlProxyHandle;
import xnet.mysqlproxy.PacketCommond;

/**
 * read init from server
 * 
 * @author quanwei
 * 
 */
public class MysqlStateRCCommond extends MysqlStateRead {
	static Log logger = LogFactory.getLog(MysqlStateRCCommond.class);

	protected MysqlStateRCCommond(MysqlProxyHandle handle, long remain) {
		super(handle, remain);

		this.rbuf = handle.getCReadBuffer();
		this.wbuf = handle.getSWriteBuffer();
		this.socket = handle.getcSocket();
		logger.debug("New State:" + MysqlStateRCCommond.class);
	}

	@Override
	public MysqlStateBase getNext() {
		return new MysqlStateWSCommond(handle,remain);
	}
	@Override
	public MysqlStateEnum getState() {
		return MysqlStateEnum.MysqlStateRCCommond;
	}
}
