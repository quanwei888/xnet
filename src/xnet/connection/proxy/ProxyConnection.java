package xnet.connection.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xnet.core.Connection; 
import xnet.core.connection.SimpleConnection; 

public class ProxyConnection extends Connection {
	static Log logger = LogFactory.getLog(SimpleConnection.class);
	/**
	 * proxy handle
	 */
	private ProxyHandle handle;

	public ProxyHandle getHandle() {
		return handle;
	}

	public void setHandle(ProxyHandle handle) {
		this.handle = handle;
	}

	@Override
	public void execute() {
		logger.debug("execute");
		handle.setcSocket(socketChannel);
		handle.setWorker(worker);
		handle.handleConnection();
	}
 
 

}
