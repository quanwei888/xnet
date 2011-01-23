package xnet.core.event;

/**
 * �¼�����
 * 
 * @author quanwei
 * 
 */
public class EventType {
	/**
	 * ��ʱ
	 */
	public final static int EV_TIMEOUT = 0x01;
	/**
	 * �ɶ�
	 */
	public final static int EV_READ = 0x02;
	/**
	 * ��д
	 */
	public final static int EV_WRITE = 0x04;
	/**
	 * ��accept
	 */
	public final static int EV_ACCEPT = 0x08;
	/**
	 * �����¼�
	 */
	public final static int EV_PERSIST = 0x10;
	/**
	 * ���ӽ���
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
