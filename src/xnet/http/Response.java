package xnet.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Response {

	Map<String, String> header = new HashMap<String, String>();
	Map<String, String> cookie = new HashMap<String, String>();
	StringBuilder res = new StringBuilder();
	String charset = "UTF-8";

	public void write(String str) {
		res.append(str);
	}

	public void reset() {
		header.clear();
		cookie.clear();
		res = new StringBuilder();
	}

	public byte[] toBytes() throws UnsupportedEncodingException {
		// body len
		byte[] body = res.toString().getBytes(charset);
		header.put(HttpAttr.HEAD_CONTENT_LEN, ((Integer) body.length).toString());

		if (!header.containsKey(HttpAttr.HEAD_CONTENT_TYPE)) {
			header.put(HttpAttr.HEAD_CONTENT_TYPE, "text/html");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 200 OK\r\n");

		Iterator<Map.Entry<String, String>> it = header.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			sb.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
		}
		sb.append("\r\n");

		byte[] head = sb.toString().getBytes(charset);
		byte[] pacaket = new byte[head.length + body.length];
		System.arraycopy(head, 0, pacaket, 0, head.length);
		System.arraycopy(body, 0, pacaket, head.length, body.length);

		return pacaket;
	}
}
