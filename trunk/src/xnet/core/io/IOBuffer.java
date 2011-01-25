package xnet.core.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class IOBuffer {
	protected ByteBuffer buf;
	protected String charset = "UTF-8";

	public ByteBuffer getBuf() {
		return buf;
	}

	public IOBuffer(int initSize) {
		buf = ByteBuffer.allocate(initSize);
	}

	public IOBuffer() {
		buf = ByteBuffer.allocate(0);
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public IOBuffer limit(int limit) {
		if (limit > buf.capacity()) {
			ByteBuffer newBuf = ByteBuffer.allocate(limit);
			System.arraycopy(buf.array(), 0, newBuf.array(), 0, buf.capacity());
			newBuf.position(buf.position());
			buf = newBuf;
		}
		buf.limit(limit);
		return this;
	}

	public IOBuffer put(byte[] src, int offset, int length) {
		buf.put(src, offset, length);
		return this;
	}

	public int limit() {
		return buf.limit();
	}

	public int position() {
		return buf.position();
	}

	public void position(int newPosition) {
		buf.position(newPosition);
	}

	public int capacity() {
		return buf.capacity();
	}

	public int remaining() {
		return buf.remaining();
	}

	public void rewind() {
		buf.rewind();
	}

	public void reset() {
		buf.reset();
	}

	public void flip() {
		buf.flip();
	}

	public void clear() {
		buf.clear();
	}



	public int readUInt8() {
		return readUInt8(buf.position());
	}

	public int readUInt8(int index) {
		return readByte(index) & 0xff;
	}

	public int readUInt16() {
		return readUInt16(buf.position());
	}

	public int readUInt16(int index) {
		return ((readByte() & 0xff) << 8) | (readByte() & 0xff);
	}

	public byte readByte() {
		return readByte(buf.position());
	}

	public byte readByte(int index) {
		buf.position(index);
		return buf.get();
	}

	public byte[] readBytes(int len) {
		return readBytes(buf.position(), len);
	}

	public byte[] readBytes(int index, int len) {
		byte[] bytes = new byte[len];
		buf.position(index);
		buf.get(bytes);
		return bytes;
	}

	public String toString() {
		return buf.toString();
	}
	
	public String getString() throws UnsupportedEncodingException {
		int pos = buf.position();
		int len = pos;
		byte[] bytes = new byte[len];
		buf.position(0);
		buf.get(bytes, 0, len);
		buf.position(pos);
		return new String(bytes, charset);
	}

	public void putString(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes(charset);
		limit(buf.position() + bytes.length);
		buf.put(bytes);
	}

}
