package xnet.demo.echoserver;

import org.apache.log4j.PropertyConfigurator;

import xnet.core.server.*;
import xnet.demo.echoclient.EchoClient;

public class EchoServer {
	public static void main(String[] args) throws Exception {
		Config config = new Config();
		config.session = EchoSession.class;
		PropertyConfigurator.configure("./conf/log4j.properties");

		if (args.length != 2) {
			System.out.println(EchoClient.class + " PORT THREAD_NUM\n");
			return;
		}

		config.threadNum = Integer.parseInt(args[0]);
		config.port = Short.parseShort(args[1]);
		config.rTimeout = 10000;
		config.wTimeout = 10000;
		config.ip = "0.0.0.0";
		config.keepalive = true;
		config.maxConnection = 1000;
		Server server = new Server(config);
		server.run();
	}
}
