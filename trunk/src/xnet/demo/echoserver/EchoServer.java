package xnet.demo.echoserver;

import org.apache.log4j.PropertyConfigurator;

import xnet.core.server.*;

public class EchoServer {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println(EchoServer.class + " PORT THREAD_NUM\n");
			return;
		}

		Config config = new Config();
		config.session = EchoSession.class;
		PropertyConfigurator.configure("./conf/log4j.properties");
		config.threadNum = Integer.parseInt(args[1]);
		config.port = Short.parseShort(args[0]);
		config.rTimeout = 10000;
		config.wTimeout = 10000;
		config.ip = "0.0.0.0";
		config.keepalive = true;
		config.maxConnection = 1000;
		Server server = new Server(config);
		server.run();
	}
}
