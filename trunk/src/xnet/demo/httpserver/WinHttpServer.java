package xnet.demo.httpserver;

import org.apache.log4j.PropertyConfigurator;

import xnet.http.HttpSession;
import xnet.http.ServletFactory;
import xnet.http.ServletFilter;
import xnet.server.*;

public class WinHttpServer {
	public static void main(String[] args) throws Exception {
		Config config = new Config();
		config.session = HttpSession.class;
		PropertyConfigurator.configure("Z:\\xnet\\conf\\log4j.properties.test");

		config.threadNum = 4;
		config.port = 8480;
		config.rTimeout = 1000;
		config.wTimeout = 1000;
		config.ip = "0.0.0.0";
		config.keepalive = false;
		config.maxConnection = 1000;
		Server server = new Server(config);
		ServletFactory.filter = (ServletFilter) new Filter();
		server.run();
	}
}
