package xnet.demo.echoclient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import xnet.client.Connection;

public class EchoClient {
	public static void main(String[] args) throws Exception {
		int threadNum = 1;
		int requests = 200;
		if (args.length == 2) {
			threadNum = Integer.parseInt(args[0]);
			requests = Integer.parseInt(args[1]);
		}
		long stime = System.currentTimeMillis();
		Thread[] clients = new Thread[threadNum];
		for (int i = 0; i < threadNum; i++) {
			TClient client = new TClient();
			client.requests = requests / threadNum;
			clients[i] = new Thread(client);
			clients[i].start();
		}
		for (int i = 0; i < threadNum; i++) {
			clients[i].join();
		}
		System.out.println(requests / ((System.currentTimeMillis() - stime) / 1000) + "/s");
	}

	static class TClient implements Runnable {
		public int requests = 100;

		public void run() {
			try {
				System.out.println("begin");
				List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
				servers.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8401));

				Connection conn = new Connection(servers, 0, 0, 0);
				conn.connect();

				int i = 0;
				while (i++ < requests) {
					String header = "hello world\n";
					byte[] stream = header.getBytes("UTF-8");
					conn.write(stream);
					String ret = new String(conn.read(header.length()), "UTF-8");
				}
				System.out.println("end");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
