package xnet.test.proxy.mysql;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import xnet.mysqlproxy.MysqlProxyFactory;
import xnet.mysqlproxy.MysqlProxyHandle;

public class Server {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("USAGE:xnet.test.proxy.mysql.Server port server_ip server_port logconfig");
			System.out.println(args.length);
			return;
		}
		PropertyConfigurator.configure(args[3]);
		xnet.core.model.Server server = new xnet.core.model.Server();
		server.setPort(Integer.parseInt(args[0]));
		MysqlProxyHandle.host = args[1];
		MysqlProxyHandle.port = Integer.parseInt(args[2]);
		server.setThreadNum(8);
		server.setConnectionFactory(new MysqlProxyFactory());
		server.run();
	}
}