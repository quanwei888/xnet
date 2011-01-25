package xnet.test.proxy.mysql;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import xnet.connection.proxy.mysql.MysqlProxyFactory;
import xnet.connection.proxy.mysql.MysqlProxyHandle;

public class Server {
	public static void main(String[] args) throws IOException {
		if (args.length != 5) {
			System.out.println("USAGE:xnet.test.proxy.mysql.Server port server_ip server_port logconfig");
			System.out.println(args);
			return;
		}
		PropertyConfigurator.configure(args[4]);
		xnet.core.model.Server server = new xnet.core.model.Server();
		server.setPort(Integer.parseInt(args[1]));
		MysqlProxyHandle.host = args[2];
		MysqlProxyHandle.port = Integer.parseInt(args[3]);
		server.setThreadNum(1);
		server.setConnectionFactory(new MysqlProxyFactory());
		server.run();
	}
}