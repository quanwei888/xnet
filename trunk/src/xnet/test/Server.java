package xnet.test;

import java.io.IOException;
 
import org.apache.log4j.PropertyConfigurator;

import xnet.connection.http.HttpFactory;

public class Server {
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("/home/quanwei/jworkspace/xnet/conf/log4j.properties");  
		xnet.core.model.Server server = new xnet.core.model.Server();
		server.setPort(8123);
		server.setThreadNum(8);
		server.setConnectionFactory(new HttpFactory());
		server.run();
	}
}