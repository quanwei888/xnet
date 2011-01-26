package xnet.core.event;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �¼��������������첽IO����
 * 
 * @author quanwei
 * 
 */
public class EventManager {
	static Log logger = LogFactory.getLog(EventManager.class);

	/**
	 * Selector��ÿ��EventManager��Ψһ���̲߳���ȫ
	 */
	protected Selector selector;
	/**
	 * key��������С�ĳ�ʱ����
	 */
	protected long minTimeOut;
	/**
	 * ��ע���key����
	 */
	protected Set<EventAttr> addSet = new HashSet<EventAttr>();
	protected Set<EventAttr> timeoutSet = new HashSet<EventAttr>();

	/**
	 * ���캯��
	 * 
	 * @throws IOException
	 */
	public EventManager() throws IOException {
		selector = Selector.open();
		minTimeOut = 0;
	}

	/**
	 * ע��һ�������¼�
	 * 
	 * @param socket
	 *            Ҫ������channel
	 * @param type
	 *            �¼�����
	 * @param evHandle
	 *            �������������¼�����ʱ�ص�
	 * @param obj
	 *            ����
	 * @param timeout
	 *            ��ʱʱ��
	 * @throws ClosedChannelException
	 */
	public void addEvent(SelectableChannel socket, int type, IEventHandle evHandle, Object obj, long timeout) {
		logger.debug("add event:" + socket);
		EventAttr attr = new EventManager.EventAttr(type, evHandle, timeout, obj, socket);
		addSet.add(attr);
		if (timeout > 0) {
			// ���뵽��ʱ�����У����ڳ�ʱ�ж�
			timeoutSet.add(attr);
		}
	}

	/**
	 * ɾ��һ�������¼�
	 * 
	 * @param socket
	 */
	public void delEvent(SelectableChannel socket) {
		logger.debug("cancel key");
		SelectionKey key = socket.keyFor(selector);
		if (key != null) {
			key.cancel();
		}
	}

	/**
	 * ����keys��ɾ��cancel״̬��key����������Ҫ���ӵ�key
	 * 
	 * @throws IOException
	 */
	protected void updateKeys() throws IOException {
		// ɾ��cancel״̬��key
		selector.selectNow();
		// ��������Ҫ���ӵ�key
		Iterator<EventAttr> it = addSet.iterator();
		while (it.hasNext()) {
			EventAttr attr = it.next();
			it.remove();

			int evSet = 0;
			if ((attr.type & EventType.EV_READ) > 0) {
				evSet = evSet | SelectionKey.OP_READ;
			}
			if ((attr.type & EventType.EV_WRITE) > 0) {
				evSet = evSet | SelectionKey.OP_WRITE;
			}
			if ((attr.type & EventType.EV_ACCEPT) > 0) {
				evSet = evSet | SelectionKey.OP_ACCEPT;
			}
			if (attr.timeout > 0 && attr.timeout < this.minTimeOut) {
				// ȡ��С�ĳ�ʱ����
				this.minTimeOut = attr.timeout;
			}
			try {
				attr.socket.register(selector, evSet, attr);
			} catch (Exception e) {
				logger.fatal(e.getStackTrace());
				attr.socket.keyFor(selector).cancel();
				try {
					attr.socket.close();
				} catch (IOException e1) {
					logger.fatal(e.getStackTrace());
				}
			}
		}
	}

	/**
	 * ��������ѯ����ע����¼���ֱ���¼�������Ϊ��
	 * 
	 * @throws IOException
	 */
	public void loop() {
		while (true) {
			try {
				updateKeys();
				if (selector.keys().size() == 0) {
					// û���¼������������ѭ��
					break;
				}
				long stime = System.currentTimeMillis();
				int ret = selector.select(minTimeOut);
				long timeCost = System.currentTimeMillis() - stime;
				logger.debug("select return:" + ret);

				// IO�¼�����
				Iterator<SelectionKey> eventIter = selector.selectedKeys().iterator();
				while (eventIter.hasNext()) {
					SelectionKey key = eventIter.next();
					eventIter.remove();
					if (!key.isValid()) {
						key.cancel();
						continue;
					}					 
					
					EventAttr attr = (EventAttr) key.attachment();
					timeoutSet.remove(attr);// �ӳ�ʱkey�������Ƴ�
					int evSet = 0;
					if (key.isReadable()) {
						evSet = evSet | EventType.EV_READ;
						logger.debug("event:EV_READ");
					}
					if (key.isWritable()) {
						evSet = evSet | EventType.EV_WRITE;
						logger.debug("event:EV_WRITE");
					}
					if (key.isAcceptable()) {
						evSet = evSet | EventType.EV_ACCEPT;
						logger.debug("event:EV_ACCEPT");
					}
					if ((attr.type & EventType.EV_PERSIST) > 0) {
						addSet.add(attr);
					}
					key.cancel();
					// �ص�����
					attr.evHandle.onIOEvent(key.channel(), evSet, attr.obj);
				}

				// ��ʱ�¼�����
				Iterator<EventAttr> timeEventIter = timeoutSet.iterator();
				while (timeEventIter.hasNext()) {
					EventAttr attr = timeEventIter.next();
					if (timeCost >= attr.timeout) {
						timeEventIter.remove();
						attr.evHandle.onIOEvent(attr.socket, EventType.EV_TIMEOUT, attr.obj);
						//ȡ�����¼�
						attr.socket.keyFor(selector).cancel();
					}
				}
			} catch (IOException e) {
				logger.warn(e.getStackTrace());
			}

		}
	}

	/**
	 * �¼����ԣ���װ�˳�ʱ���¼����͡�����
	 * 
	 * @author quanwei
	 * 
	 */
	class EventAttr {
		public int type;
		public IEventHandle evHandle;
		public long timeout;
		public Object obj;
		public SelectableChannel socket;

		public EventAttr(int type, IEventHandle evHandle, long timeout, Object obj, SelectableChannel socket) {
			super();
			this.type = type;
			this.evHandle = evHandle;
			this.timeout = timeout;
			this.obj = obj;
			this.socket = socket;
		}

	}

}
