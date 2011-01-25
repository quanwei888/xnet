package xnet.core.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	public void execute() {
		logger.debug("new connection");
		handle.setcSocket(socket);
		handle.setWorker(worker);
		handle.handleConnection();
	}

}
