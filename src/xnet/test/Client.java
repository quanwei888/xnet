package xnet.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static void main(String[] args) throws UnknownHostException,
			IOException, InterruptedException {
		String ip = "127.0.0.1";
		int port = 8123;
		try {
			while (true) {
				Socket socket = new Socket(ip, port);
				socket.setSoTimeout(5539900);
				java.io.OutputStream out = socket.getOutputStream();
				byte[] data = "GET http://127.0.0.1:8123/ HTTP/1.1\r\n\r\n"
						.getBytes();
				out.write(data);
				out.flush();

				socket.shutdownOutput();

				byte[] buffer = new byte[1024];
				int len = -1; 
				java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();

				java.io.InputStream in = socket.getInputStream();

				while ((len = in.read(buffer, 0, buffer.length)) > 0) {
					bout.write(buffer, 0, len);
				}
				in.close();
				bout.flush();
				bout.close();

				byte[] rdata = bout.toByteArray();
				// System.out.println("leen = " + (rdata.length - 32));
				System.out.println(new String(rdata));
 
				//socket.close();
				Thread.sleep(1000);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
