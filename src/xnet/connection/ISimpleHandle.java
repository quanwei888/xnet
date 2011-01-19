package xnet.connection;

import xnet.io.IOBuffer;


public interface ISimpleHandle {
	public int remain(IOBuffer buf);
	public void handle(IOBuffer reqBuf,IOBuffer resBuf) throws Exception;
}
