package xnet.connection.proxy.mysql;

import xnet.core.IOBuffer;

public class MysqlIOBuffer extends IOBuffer {
	public int getBodyLen() {
		return getBodyLen(0);
	}

	public int getBodyLen(int index) {
		byte[] header = new byte[3];
		int pos = buf.position();
		buf.position(index);
		buf.get(header);
		int size = (header[0] & 0xff) | ((header[1] & 0xff) << 8)
				| ((header[2] & 0xff) << 16);
		buf.position(pos);
		return size;
	}

	public byte[] getByteString(int index) {
		int endIndex = index;
		while (true) {
			if (buf.limit() <= endIndex) {
				return null;
			}
			if (buf.get(endIndex) != 0) {
				break;
			}
			endIndex++;
		}
		int len = endIndex - index;
		byte[] bytes = new byte[endIndex - index];
		buf.get(bytes, index, len);
		return bytes;
	}
}
