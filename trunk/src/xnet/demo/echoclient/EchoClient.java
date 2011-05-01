package xnet.demo.echoclient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import xnet.core.client.Connection;

public class EchoClient {
	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println(EchoClient.class + " IP PORT MESSAGE\n");
			return;
		}

		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
		servers.add(new InetSocketAddress(InetAddress.getByName(args[0]), Short
				.parseShort(args[1])));

		Connection conn = new Connection(servers, 0, 0, 0);
		conn.connect();

		String header = args[2] + "\n";
		byte[] stream = header.getBytes("UTF-8");
		conn.write(stream);
		String ret = new String(conn.read(header.length()), "UTF-8");
		System.out.println("receive from server:\n" + ret);
	}
}
