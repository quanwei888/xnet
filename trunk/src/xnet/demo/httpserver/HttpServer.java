package xnet.demo.httpserver;

import org.apache.log4j.PropertyConfigurator;

import xnet.core.http.HttpSession;
import xnet.core.http.ServletFactory;
import xnet.core.http.ServletFilter;
import xnet.core.server.*;

public class HttpServer {
	public static void main(String[] args) throws Exception {
		Config config = new Config();
		config.session = HttpSession.class;
		PropertyConfigurator.configure("./conf/log4j.properties");

		config.threadNum = Runtime.getRuntime().availableProcessors() + 1;
		config.port = 8480;
		config.rTimeout = 1000;
		config.wTimeout = 1000;
		config.ip = "0.0.0.0";
		config.keepalive = true;
		config.maxConnection = 1000;
		Server server = new Server(config);
		ServletFactory.filter = (ServletFilter) new Filter();
		server.run();
	}
}
