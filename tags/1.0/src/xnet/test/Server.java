package xnet.test;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import xnet.core.connection.ConnectionPool;
import xnet.http.HttpFactory;

public class Server {
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("/home/quanwei/jworkspace/xnet/conf/log4j.properties");
		ConnectionPool.setConnFactory(new HttpFactory());
		xnet.core.model.Server server = new xnet.core.model.Server();
		server.setPort(8123);
		server.setThreadNum(8);
		server.run();
	}
}