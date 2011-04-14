package xnet.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;

import xnet.client.Connection;

import junit.framework.TestCase;

public class TestConnection extends TestCase {
	public void testConnect() throws Exception {
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
		servers.add(new InetSocketAddress(InetAddress.getByName("tc-sf-ra04.tc.baidu.com"), 8298));
		Connection conn = new Connection(servers, 1000, 0, 0);
		conn.connect();
		String header = "GET /login.php HTTP/1.1\r\nHost: tc-sf-ra04.tc.baidu.com:8298\r\n\r\n";
		byte[] stream = header.getBytes("UTF-8");
		conn.write(stream);

		while (true) {
			System.out.print(new String(conn.read(100), "UTF-8"));
		}
	}
}
