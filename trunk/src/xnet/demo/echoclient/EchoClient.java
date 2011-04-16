package xnet.demo.echoclient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import xnet.client.Connection;

public class EchoClient {
	public static void main(String[] args) throws Exception {
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
		servers.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8401));

		Connection conn = new Connection(servers, 0, 0, 0);
		conn.connect();

		String header = "hello world\n";
		if (args.length > 0) {
			header = args[1] + "\n";
		}
		byte[] stream = header.getBytes("UTF-8");
		conn.write(stream);
		String ret = new String(conn.read(header.length()), "UTF-8");
		System.out.println("receive from server:\n" + ret);
	}
}
