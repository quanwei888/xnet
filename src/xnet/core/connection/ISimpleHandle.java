package xnet.core.connection;

import xnet.core.IOBuffer;

public interface ISimpleHandle {
	public int remain(IOBuffer buf);

	/**
	 * session����
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionCreate(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * һ��session�Ự��ʼ
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionOpen(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * һ��session�Ự����
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionHandle(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * һ��session�Ự����
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionClose(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;

	/**
	 * session�ر�
	 * 
	 * @param reqBuf
	 * @param resBuf
	 */
	public void sessionDestrory(IOBuffer reqBuf, IOBuffer resBuf) throws Exception;
}
