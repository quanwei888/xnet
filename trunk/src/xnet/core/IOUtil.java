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
	 * @param socket
	 * @param buf
	 * @return
	 */
	public static IOState nread(SocketChannel socket, IOBuffer readBuf) {
		try {
			int remain = readBuf.remaining();
			int len = socket.read(readBuf.getBuf());
			logger.debug("read from " + socket.socket().getRemoteSocketAddress() + ",remain:" + remain + ",read len:" + len);
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
	public static IOState nwrite(SocketChannel socket, IOBuffer writeBuf) {
		try {
			int remain = writeBuf.remaining();
			int len = socket.write(writeBuf.getBuf());
			logger.debug("write to " + socket.socket().getRemoteSocketAddress() +",remain:" + remain + ",write len:" + len);

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
