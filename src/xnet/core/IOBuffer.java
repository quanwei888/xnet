package xnet.core;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 用于非阻塞IO的buffer
 * 
 * @author quanwei
 * 
 */
public class IOBuffer {
	ByteBuffer buf;

	public ByteBuffer getBuf() {
		return buf;
	}

	public IOBuffer(int initSize) {
		buf = ByteBuffer.allocate(initSize);
	}

	public IOBuffer() {
		buf = ByteBuffer.allocate(0);
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

	public void compact() {
		buf.compact();
	}

	public byte readByte(int index) {
		buf.position(index);
		return buf.get();
	}

	public byte readByte() {
		return readByte(buf.position());
	}

	public byte[] readBytes() {
		return readBytes(buf.limit());
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

	public byte getByte(int index) {
		int pos = buf.position();
		buf.position(index);
		byte b = buf.get();
		buf.position(pos);
		return b;
	}

	public byte getByte() {
		return getByte(buf.position());
	}

	public byte[] getBytes() {
		return getBytes(buf.limit());
	}

	public byte[] getBytes(int len) {
		return getBytes(buf.position(), len);
	}

	public byte[] getBytes(int index, int len) {
		int pos = buf.position();
		byte[] bytes = new byte[len];
		buf.position(index);
		buf.get(bytes);
		buf.position(pos);
		return bytes;
	}

	public IOBuffer writeBytes(byte[] bytes) {
		return writeBytes(buf.position(), bytes);
	}

	public IOBuffer writeBytes(int index, byte[] bytes) {
		buf.position(index);
		limit(index + bytes.length);
		buf.put(bytes);
		return this;
	}

	public String readString(int len, String charset) {
		return readString(buf.position(), len, charset);
	}

	public String readString(int index, int len, String charset) {
		buf.position(index);
		byte[] bytes = readBytes(len);
		try {
			return new String(bytes, charset);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public String getString(String charset) {
		return getString(0, buf.position(), charset);
	}

	public String getString(int len, String charset) {
		return getString(buf.position(), len, charset);
	}

	public String getString(int index, int len, String charset) {
		int pos = buf.position();
		buf.position(index);
		byte[] bytes = readBytes(len);
		buf.position(pos);
		try {
			return new String(bytes, charset);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public void writeString(String str, String charset) {
		byte[] bytes;
		try {
			bytes = str.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			return;
		}
		limit(buf.position() + bytes.length);
		buf.put(bytes);
	}
}