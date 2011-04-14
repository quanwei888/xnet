package xnet.client;

import java.net.InetSocketAddress;

public class Config {
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
	public InetSocketAddress servers;
	/**
	 * 是否长连接
	 */
	public boolean keepalive;
}
