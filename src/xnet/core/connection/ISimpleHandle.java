package xnet.core.connection;

import xnet.core.IOBuffer;

public interface ISimpleHandle {
	public int remain(IOBuffer buf);

	/**
	 * session建立
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionCreate(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * 一次session会话开始
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionOpen(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * 一次session会话处理
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionHandle(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * 一次session会话结束
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionClose(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * session关闭
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionDestrory(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;
}
