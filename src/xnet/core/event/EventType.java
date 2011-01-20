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
}
