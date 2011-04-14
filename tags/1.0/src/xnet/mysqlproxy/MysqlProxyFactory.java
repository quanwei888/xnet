package xnet.mysqlproxy;
 
import xnet.core.connection.IConnection;
import xnet.core.connection.IConnectionFactory;
import xnet.core.connection.ProxyConnection;

public class MysqlProxyFactory implements IConnectionFactory {
	public MysqlProxyFactory() {
		
	}

	public IConnection createConnection() {
		ProxyConnection conn = new ProxyConnection();
		conn.setHandle(new MysqlProxyHandle());
		return conn;
	}
}
