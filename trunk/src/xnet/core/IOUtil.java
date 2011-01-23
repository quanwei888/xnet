package xnet.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IOUtil {
	static Log logger = LogFactory.getLog(IOUtil.class);

	/**
	 * ¥”Õ¯¬Á∂¡buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	public static IOState nread(SocketChannel socketChannel, IOBuffer readBuf) {
		try {
			int remain = readBuf.remaining();
			int len = socketChannel.read(readBuf.getBuf());
			logger.debug("remain:" + remain + ",read len:" + len);
			if (len == -1) {
				return IOState.CLOSE;
			}

			if (readBuf.remaining() == 0) {
				return IOState.COMPLATE;
			} else {
				return IOState.GOON;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return IOState.ERROR;
		}
	}

	/**
	 * ¥”Õ¯¬Á–¥buffer
	 * 
	 * @param socketChannel
	 * @param buf
	 * @return
	 */
	public static IOState nwrite(SocketChannel socketChannel, IOBuffer writeBuf) {
		try {
			int remain = writeBuf.remaining();
			int len = socketChannel.write(writeBuf.getBuf());
			logger.debug("remain:" + remain + ",write len:" + len);

			if (len == -1) {
				return IOState.CLOSE;
			}
			if (writeBuf.remaining() == 0) {
				return IOState.COMPLATE;
			} else {
				return IOState.GOON;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return IOState.ERROR;
		}
	}
}
