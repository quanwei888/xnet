package xnet.event;

/**
 * �¼����ͣ������¼����͵ĳ���
 * 
 * @author quanwei
 * 
 */
public class EventType {
	public final static int EV_TIMEOUT = 0x01;
	public final static int EV_READ = 0x02;
	public final static int EV_WRITE = 0x04;
	public final static int EV_ACCEPT = 0x08;
	public final static int EV_PERSIST = 0x10;
}
