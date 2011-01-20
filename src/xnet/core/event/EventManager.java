package xnet.core.event;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

	protected Selector selector;
	protected long timeout;

	/**
	 * ���캯��
	 * 
	 * @throws IOException
	 */
	public EventManager() throws IOException {
		selector = Selector.open();
		timeout = 0;
	}

	/**
	 * ע��һ�������¼�
	 * 
	 * @param channel
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
	public boolean addEvent(SelectableChannel channel, int type, IEventHandle evHandle, Object obj, long timeout) {
		int evSet = 0;
		if ((type & EventType.EV_READ) > 0) {
			evSet = evSet | SelectionKey.OP_READ;
		}
		if ((type & EventType.EV_WRITE) > 0) {
			evSet = evSet | SelectionKey.OP_WRITE;
		}
		if ((type & EventType.EV_ACCEPT) > 0) {
			evSet = evSet | SelectionKey.OP_ACCEPT;
		}
		if (timeout > 0 && timeout < this.timeout) {
			// ȡ��С�ĳ�ʱ����
			this.timeout = timeout;
		}

		try {
			EventAttr attr = new EventManager.EventAttr(type, evHandle, obj, timeout);
			channel.register(selector, evSet, attr);
		} catch (ClosedChannelException e) {
			logger.warn(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * ɾ��һ�������¼�
	 * 
	 * @param channel
	 */
	public void delEvent(SelectableChannel channel) {
		logger.debug("cancel key");
		SelectionKey key = channel.keyFor(selector);
		if (key != null) {
			key.cancel();
		}
	}

	/**
	 * ��������ѯ����ע����¼���ֱ���¼�������Ϊ��
	 * 
	 * @throws IOException
	 */
	public void loop() {
		while (true) {
			long stime = System.currentTimeMillis();
			int ret;
			try {
				ret = selector.select();
			} catch (IOException e) {
				logger.warn(e.getMessage());
				continue;
			}

			logger.debug("select return:" + ret);
			if (selector.keys().size() == 0) {
				// û���¼������������ѭ��
				break;
			}

			if (ret == 0) {
				// ��ʱ����
				long timeCost = System.currentTimeMillis() - stime;
				Set<SelectionKey> keys = selector.keys();
				Iterator<SelectionKey> iter = keys.iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					EventAttr attr = (EventAttr) key.attachment();
					if (attr.timeout == 0) {
						continue;
					}

					if (timeCost > attr.timeout) {
						// �¼�������
						attr.evHandle.onIOReady(key.channel(), EventType.EV_TIMEOUT, attr.obj);
						if ((attr.type & EventType.EV_PERSIST) == 0) {
							// �������EV_PERSIST���͵��¼�����ɾ��������key
							key.cancel();
						}
					}
				}
				continue;
			}

			// ���ǳ�ʱ
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iter = keys.iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();
				EventAttr attr = (EventAttr) key.attachment();

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

				// �¼�������
				attr.evHandle.onIOReady(key.channel(), evSet, attr.obj);
				if ((attr.type & EventType.EV_PERSIST) == 0) {
					key.cancel();
				}
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
		int type;
		IEventHandle evHandle;
		long timeout;
		public Object obj;

		public EventAttr(int type, IEventHandle handle, Object obj, long timeout) {
			this.type = type;
			this.obj = obj;
			this.timeout = timeout;
			this.evHandle = handle;
		}
	}
}
