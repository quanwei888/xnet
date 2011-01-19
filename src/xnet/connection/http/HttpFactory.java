package xnet.connection.http;

import xnet.connection.*;

public class HttpFactory implements IConnectionFactory {
	public IConnection createConnection() {
		SimpleConnection conn =  new SimpleConnection();
		conn.setHandle(new HttpHandle());
		return conn;
	}
}
