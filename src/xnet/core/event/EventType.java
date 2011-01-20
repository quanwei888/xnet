package xnet.core.event;

/**
 * 事件类型
 * 
 * @author quanwei
 * 
 */
public class EventType {
	/**
	 * 超时
	 */
	public final static int EV_TIMEOUT = 0x01;
	/**
	 * 可读
	 */
	public final static int EV_READ = 0x02;
	/**
	 * 可写
	 */
	public final static int EV_WRITE = 0x04;
	/**
	 * 可accept
	 */
	public final static int EV_ACCEPT = 0x08;
	/**
	 * 永久事件
	 */
	public final static int EV_PERSIST = 0x10;
}
