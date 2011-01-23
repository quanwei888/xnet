package xnet.test.proxy.mysql;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import xnet.connection.http.HttpFactory;
import xnet.connection.proxy.mysql.MysqlProxyFactory;

public class Server {
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("/home/quanwei/jworkspace/xnet/conf/log4j.properties");
		xnet.core.model.Server server = new xnet.core.model.Server();
		server.setPort(8124);
		server.setThreadNum(1);
		server.setConnectionFactory(new MysqlProxyFactory());
		server.run();
	}
}