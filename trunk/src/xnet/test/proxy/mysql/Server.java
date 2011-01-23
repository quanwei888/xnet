package xnet.test.proxy.mysql;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import xnet.connection.proxy.mysql.MysqlProxyFactory;
import xnet.connection.proxy.mysql.MysqlProxyHandle;

public class Server {
	public static void main(String[] args) throws IOException {
		// PropertyConfigurator.configure("/home/quanwei/jworkspace/xnet/conf/log4j.properties");
		BasicConfigurator.configure();
		xnet.core.model.Server server = new xnet.core.model.Server();
		MysqlProxyHandle.host = args[0];
		MysqlProxyHandle.port = Integer.parseInt(args[1]);
		server.setPort(8124);
		server.setThreadNum(1);
		server.setConnectionFactory(new MysqlProxyFactory());
		server.run();
	}
}