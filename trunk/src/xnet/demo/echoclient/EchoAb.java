package xnet.demo.echoclient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import xnet.core.client.Connection;

public class EchoAb {
	public static void main(String[] args) throws Exception {
		int threadNum = 1;
		int requests = 200;
		if (args.length != 4) {
			System.out.println(EchoAb.class + " IP PORT THREAD_NUM REQUEST_COUNT\n");
			return;
		}
		threadNum = Integer.parseInt(args[2]);
		requests = Integer.parseInt(args[3]);
		long stime = System.currentTimeMillis();
		Thread[] clients = new Thread[threadNum];
		for (int i = 0; i < threadNum; i++) {
			TClient client = new TClient();
			client.ip = args[0];
			client.port = Short.parseShort(args[1]);
			client.requests = requests / threadNum;
			clients[i] = new Thread(client);
			clients[i].start();
		}
		for (int i = 0; i < threadNum; i++) {
			clients[i].join();
		}
		System.out.println("result:" + (double) requests / (System.currentTimeMillis() - stime) * 1000 + "/s");
	}

	static class TClient implements Runnable {
		public int requests = 100;
		public String ip = "127.0.0.1";
		public short port = 8401;

		public void run() {
			try {
				System.out.println("begin");
				List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
				servers.add(new InetSocketAddress(InetAddress.getByName(ip), port));

				Connection conn = new Connection(servers, 0, 0, 0);
				conn.connect();

				int i = 0;
				while (i++ < requests) {
					try {
						String header = "hello world\n";
						byte[] stream = header.getBytes("UTF-8");
						conn.write(stream);
						conn.read(header.length());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				System.out.println("end");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
