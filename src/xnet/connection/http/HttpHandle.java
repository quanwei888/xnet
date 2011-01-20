package xnet.connection.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import xnet.core.*;
import xnet.io.IOBuffer;
import xnet.test.Test;

public class HttpHandle implements ISimpleHandle {
	static Log logger = LogFactory.getLog(HttpHandle.class);
	
	public static int PACK_SIZE = 1024;

	Map<String, String> reqHeader = null;
	int headerEndPos = 0;

	public void beginRequest(IOBuffer reqBuf, IOBuffer resBuf) throws Exception {
		reqHeader = null;
		// 初始化readbuf
		reqBuf.clear();
		reqBuf.limit(PACK_SIZE);

		// 初始化writebuf
		resBuf.clear();
		resBuf.limit(0);
	}

	public void doRequest(IOBuffer reqBuf, IOBuffer resBuf) throws Exception {
		//logger.debug(reqHeader);
		String res = "";
		res += "HTTP/1.1 200 OK\r\n";
		res += "Content-Length: 11\r\n";
		res += "Version: HTTP/1.1\r\n";
		res += "Content-Type: text/html\r\n\r\n";
		res += "hello world";
		resBuf.putString(res, "ISO-8859-1");
		reqHeader.remove("Cookie");
		logger.info(reqHeader + "\t" + res.length() + "\t" + "200");
		
		/**
		header.put("Content-Type", "text/html");
		header.put("Version", "HTTP/1.0");
		header.put("Code", "200");
		header.put("Status", "OK");
		header.put("Body", "hello world");
		logger.debug(header);
		buildHeader(resBuf, header);
		*/
	}

	public int remain(IOBuffer buf) {
		if (reqHeader == null) {
			String sHeader = buf.toString("ISO-8859-1");
			int pos = sHeader.indexOf("\r\n\r\n");
			if (pos >= 0) {
				sHeader = sHeader.substring(0, pos);
				reqHeader = parseHeader(sHeader);
				headerEndPos = pos;
			} else {
				if (buf.remaining() > 0) {
					return buf.limit();
				} else {
					return buf.limit() + PACK_SIZE;
				}
			}
		}

		if (reqHeader == null) {
			// 非法header
			return -1;
		}

		if (reqHeader.get("Method").endsWith("POST")) {
			// post方法
			if (!reqHeader.containsKey("Content-Length")) {
				// 没有Content-Length
				return -1;
			}
			int contentLen = Integer.parseInt(reqHeader.get("Content-Length"));
			if (contentLen < 0) {
				// Content-Length < 0
				return -1;
			}

			int totalLen = headerEndPos + 4 + contentLen;
			if (totalLen > buf.limit()) {
				return totalLen;
			}
		}
		return 0;
	}

	protected void buildHeader(IOBuffer resBuf, Map<String, String> header)
			throws Exception {
		if (!header.containsKey("Version") || !header.containsKey("Code")
				|| !header.containsKey("Status")) {
			// 非法header
			throw new Exception();
		}
		String hLine = header.get("Version") + " " + header.get("Code") + " "
				+ header.get("Status");
		resBuf.putString(hLine, "ISO-8859-1");
		resBuf.putString("\r\n", "ISO-8859-1");
		header.remove("Method");
		header.remove("Code");
		header.remove("Status");

		String body = "";
		if (header.containsKey("Body")) {
			body = header.get("Body");
			header.remove("Body");
			header.put("Content-Length", String
					.valueOf(body.getBytes("utf-8").length));
		}
		Iterator<Map.Entry<String, String>> it = header.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it
					.next();
			resBuf.putString(entry.getKey() + ": " + entry.getValue(),
					"ISO-8859-1");
			resBuf.putString("\r\n", "ISO-8859-1");
		}
		resBuf.putString("\r\n", "ISO-8859-1");
		resBuf.putString(body, "ISO-8859-1");
	}

	protected Map<String, String> parseHeader(String sHeader) {
		Map<String, String> header = new HashMap<String, String>();
		String[] lines = sHeader.split("\r?\n");

		if (lines.length > 0) {
			String hline = lines[0];
			String[] row = hline.split(" ");
			if (row.length != 3) {
				return null;
			}

			if (!row[0].equals("GET") && !row[0].equals("POST")) {
				return null;
			}

			header.put("Method", row[0].trim());
			header.put("Url", row[1].trim());
			header.put("Version", row[2].trim());
		}

		for (String line : lines) {
			String[] row = line.split(": ");
			if (row.length != 2) {
				continue;
			}
			header.put(row[0].trim(), row[1].trim());
		}
		return header;
	}

}
