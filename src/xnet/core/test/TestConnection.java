package xnet.core.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;

import xnet.core.client.Connection;

import junit.framework.TestCase;

public class TestConnection extends TestCase {
	public void testConnect() throws Exception {
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
		servers.add(new InetSocketAddress(InetAddress.getByName("www.google.com"), 80));
		Connection conn = new Connection(servers, 1000, 1000, 0);
		conn.connect();
		String header = "GET / HTTP/1.1\r\nHost: www.google.com\r\n\r\n";
		byte[] stream = header.getBytes("UTF-8");
		conn.write(stream);

		while (true) {
			System.out.print(new String(conn.read(1), "UTF-8"));
		}
	}
}
