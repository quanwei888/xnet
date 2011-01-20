package xnet.core.connection;

import xnet.core.IOBuffer;


public interface ISimpleHandle {
	public int remain(IOBuffer buf);
	public void beginRequest(IOBuffer reqBuf,IOBuffer resBuf) throws Exception;
	public void doRequest(IOBuffer reqBuf,IOBuffer resBuf) throws Exception;
}
