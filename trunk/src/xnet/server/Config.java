package xnet.server;

/**
 * 全局配置
 * 
 * @author quanwei
 * 
 */
public class Config {
	/**
	 * 工作线程数
	 */
	public int threadNum;
	/**
	 * 端口
	 */
	public int port;
	/**
	 * 读超时（ms）
	 */
	public int rTimeout;
	/**
	 * 写超时（ms）
	 */
	public int wTimeout;
	/**
	 * IP
	 */
	public String ip;
	/**
	 * 最大连接数
	 */
	public int maxConnection;
	/**
	 * session类
	 */
	public Class<?> session = null;
	/**
	 * 是否长连接
	 */
	public boolean keepalive;

}
