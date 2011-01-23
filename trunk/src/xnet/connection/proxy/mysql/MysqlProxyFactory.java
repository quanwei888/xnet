package xnet.connection.proxy.mysql;

import xnet.connection.proxy.ProxyConnection;
import xnet.core.*; 

public class MysqlProxyFactory implements IConnectionFactory {
	public MysqlProxyFactory() {
		
	}

	public IConnection createConnection() {
		ProxyConnection conn = new ProxyConnection();
		conn.setHandle(new MysqlProxyHandle());
		return conn;
	}
}
