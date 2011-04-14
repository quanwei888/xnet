package xnet.mysqlproxy;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import xnet.core.connection.ConnectionPool;
import xnet.core.model.Server;

public class MysqlProxy {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("USAGE:xnet.test.proxy.mysql.Server port server_ip server_port logconfig");
			System.out.println(args.length);
			return;
		}
		ConnectionPool.setConnFactory(new MysqlProxyFactory());
		PropertyConfigurator.configure(args[3]);
		Server server = new Server();
		server.setPort(Integer.parseInt(args[0]));
		MysqlProxyHandle.host = args[1];
		MysqlProxyHandle.port = Integer.parseInt(args[2]);
		server.setThreadNum(1);
		 
		server.run();
	}
}