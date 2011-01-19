package xnet.test;

import java.io.IOException;

import xnet.connection.http.HttpFactory;

public class Server {
	public static void main(String[] args) throws IOException {
		xnet.server.Server server = new xnet.server.Server();
		server.setPort(8123);
		server.setThreadNum(8);
		server.setConnectionFactory(new HttpFactory());
		server.run();
	}
}