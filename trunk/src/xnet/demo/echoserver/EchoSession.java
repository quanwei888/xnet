package xnet.demo.echoserver;

import xnet.core.IOBuffer;
import xnet.server.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EchoSession extends Session {
	static Log logger = LogFactory.getLog(EchoSession.class);

	static final int BUF_SIZE = 1024;

	@Override
	public void close() throws Exception {
		logger.debug("DEBUG ENTER");
	}

	@Override
	public int getInitReadBuf() {
		return BUF_SIZE;
	}

	@Override
	public void handle(IOBuffer readBuf, IOBuffer writeBuf) throws Exception {
		//logger.info("DEBUG ENTER");
		writeBuf.writeBytes(readBuf.readBytes());
	}

	@Override
	public void open() throws Exception {
		logger.debug("DEBUG ENTER");
	}

	@Override
	public int remain(IOBuffer readBuf) throws Exception {
		if (readBuf.position() > 1) {
			byte b = readBuf.getByte(readBuf.position() - 1);
			if (b == (byte) '\n') {
				return 0;
			}
		}
		return BUF_SIZE;
	}

	@Override
	public void timeout() throws Exception {
		logger.debug("DEBUG ENTER");
	}
}
