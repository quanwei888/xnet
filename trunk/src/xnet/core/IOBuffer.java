package xnet.core;

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

	public byte get() {
		return buf.get();
	}

	public byte get(int index) {
		return buf.get(index);
	}

	public char getChar() {
		return buf.getChar();
	}

	public char getChar(int index) {
		return buf.getChar(index);
	}

	public double getDouble() {
		return buf.getDouble();
	}

	public double getDouble(int index) {
		return buf.getDouble(index);
	}

	public float getFloat() {
		return buf.getFloat();
	}

	public float getFloat(int index) {
		return buf.getFloat(index);
	}

	public int getInt() {
		return buf.getInt();
	}

	public int getInt(int index) {
		return buf.getInt(index);
	}

	public long getLong() {
		return buf.getLong();
	}

	public long getLong(int index) {
		return buf.getLong(index);
	}

	public short getShort() {
		return buf.getShort();
	}

	public short getShort(int index) {
		return buf.getShort(index);
	}

}
