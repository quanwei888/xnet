package xnet.connection.http;

import xnet.core.*;
import xnet.core.connection.SimpleConnection;

public class HttpFactory implements IConnectionFactory {
	public IConnection createConnection() {
		SimpleConnection conn =  new SimpleConnection();
		conn.setHandle(new HttpHandle());
		return conn;
	}
}
