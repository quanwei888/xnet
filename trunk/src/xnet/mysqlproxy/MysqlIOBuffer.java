package xnet.mysqlproxy;

import xnet.core.io.IOBuffer;

public class MysqlIOBuffer extends IOBuffer {
	public int getBodyLen() {
		return getBodyLen(0);
	}

	public int getBodyLen(int index) {
		byte[] header = new byte[3];
		int pos = buf.position();
		buf.position(index);
		buf.get(header);
		int size = (header[0] & 0xff) | ((header[1] & 0xff) << 8) | ((header[2] & 0xff) << 16);
		buf.position(pos);
		return size;
	}

	public int readSize(int index) {
		buf.position(index);
		byte[] bytes = readBytes(3);
		int size = (bytes[0] & 0xff) | ((bytes[1] & 0xff) << 8) | ((bytes[2] & 0xff) << 16);
		return size;
	}

	public long readLCB() {
		return readLCB(buf.position());
	}

	public long readLCB(int index) {
		buf.position(index);
		int mark = readUInt8();
		if (mark <= 250) {
			return mark;
		} else if (mark == 251) {
			return -1;
		} else if (mark == 252) {
			return ((readByte() & 0xff) << 8) | (readByte() & 0xff);
		} else if (mark == 253) {
			return ((readByte() & 0xff) << 16) | ((readByte() & 0xff) << 8) | (readByte() & 0xff);
		} else if (mark == 254) {
			// @todo
			return -1;
		}
		return -1;
	}

	public PacketResult getResultPacket() {
		int pos = buf.position();
		PacketResult result = readResultPacket(buf.position());
		buf.position(pos);
		return result;
	}

	public PacketResult readResultPacket() {
		return readResultPacket(buf.position());
	}

	public PacketResult readResultPacket(int index) {
		buf.position(index);
		int size = readSize(index);
		readByte();
		int type = readUInt8();
		if (type == PacketResult.TYPE_OK) {
			PacketResultOk result = new PacketResultOk();
			result.type = type;
			result.accectedRows = readLCB();
			result.insertId = readLCB();
			result.serverStatus = readUInt16();
			int remain = index + 4 + size - buf.position();
			result.message = readString(remain);
			return result;
		} else if (type == PacketResult.TYPE_ERR) {
			PacketResultError result = new PacketResultError();
			result.type = type;
			result.errno = (readByte() & 0xff) | ((readByte() & 0xff) << 8);
			return result;
		} else if (type == PacketResult.TYPE_EOF) {
			PacketResultEof result = new PacketResultEof();
			result.type = type;
			result.warningCount = readUInt16();
			result.serverStatus = readUInt16();
			return result;
		} else {
			PacketResultData result = new PacketResultData();
			result.type = type;
			buf.position(buf.position() - 1);// »ØÍË1byte
			result.fieldCount = readLCB();
			int remain = index + 4 + size - buf.position();
			result.extra = remain > 0 ? readLCB() : 0;
			return result;
		}
	}

	public boolean nextPacket() {
		return nextPacket(buf.position());
	}

	public boolean nextPacket(int index) {
		buf.position(index);
		int size = readSize(index);
		if (index + 4 + size <= buf.limit()) {
			buf.position(index + 4 + size);
			return true;
		} else {
			return false;
		}
	}

	public byte[] readNextPacket() throws Exception {
		return readNextPacket(buf.position());
	}

	public byte[] readNextPacket(int index) throws Exception {
		buf.position(index);
		int size = readSize(index);
		if (index + 4 + size <= buf.limit()) {
			buf.position(index);
			return readBytes(4 + size);
		}
		throw new Exception("index overflow");
	}

	public boolean hasNextPacket() {
		return hasNextPacket(buf.position());
	}

	public boolean hasNextPacket(int index) {
		if (index + 4 > buf.limit()) {
			return false;
		}
		buf.position(index);
		int size = readSize(index);
		buf.position(index);
		if (index + 4 + size <= buf.limit()) {
			return true;
		} else {
			return false;
		}
	}

	public MysqlIOBuffer writeBytes(byte[] bytes) {
		return writeBytes(buf.position(), bytes);
	}

	public MysqlIOBuffer writeBytes(int index, byte[] bytes) {
		buf.position(index);
		limit(index + bytes.length);
		buf.put(bytes);
		return this;
	}

	public PacketCommond readPacketCommond() {
		return readPacketCommond(buf.position());
	}

	public PacketCommond readPacketCommond(int index) {
		buf.position(index);
		int size = readSize(index);
		readByte();
		int type = readUInt8();
		PacketCommond commond = new PacketCommond();
		commond.type = type;
		int len = index + 4 + size - buf.position();
		commond.cmd = readString(len);
		return commond;
	}

}
