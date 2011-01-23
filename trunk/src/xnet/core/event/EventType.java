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
	/**
	 * 链接建立
	 */
	public final static int EV_CONNECT = 0x20;

	public static String getString(int type) {
		switch (type) {
		case EV_TIMEOUT:
			return "EV_TIMEOUT";
		case EV_READ:
			return "EV_READ";
		case EV_WRITE:
			return "EV_WRITE";
		case EV_ACCEPT:
			return "EV_ACCEPT";
		case EV_PERSIST:
			return "EV_PERSIST";
		case EV_CONNECT:
			return "EV_CONNECT";
		}
		return "";
	}
}
