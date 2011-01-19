package xnet.event;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * �¼��������������첽IO����
 * 
 * @author quanwei
 * 
 */
public class EventManager {
	protected Selector selector;
	protected long timeOut;

	/**
	 * ���캯��
	 * 
	 * @throws IOException
	 */
	public EventManager() throws IOException {
		selector = Selector.open();
		timeOut = 0;
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
	 * @param timeOut
	 *            ��ʱʱ��
	 * @throws ClosedChannelException
	 */
	public void addEvent(SelectableChannel channel, int type,
			IEventHandle evHandle, Object obj, long timeOut)
			throws ClosedChannelException {
		EventAttr attr = new EventManager.EventAttr(type, evHandle, obj,
				timeOut);

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
		if (timeOut > 0 && timeOut < this.timeOut) {
			// ȡ��С�ĳ�ʱ����
			this.timeOut = timeOut;
		}

		channel.register(selector, evSet, attr);
	}

	/**
	 * ɾ��һ�������¼�
	 * 
	 * @param channel
	 */
	public void delEvent(SelectableChannel channel) {
		channel.keyFor(selector).cancel();
		System.out.println("cancel key");
	}

	/**
	 * ��������ѯ����ע����¼���ֱ���¼�������Ϊ��
	 * 
	 * @throws IOException
	 */
	public void loop() throws IOException {
		while (true) {
			long stime = System.currentTimeMillis();
			int ret = selector.select();
			System.out.println("select return:" + ret);
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
					if (attr.timeOut == 0) {
						continue;
					}

					if (timeCost > attr.timeOut) {
						// �¼�������
						attr.evHandle.handle(key.channel(),
								EventType.EV_TIMEOUT, attr.obj);
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
					System.out.println("event:EV_READ");
				}
				if (key.isWritable()) {
					evSet = evSet | EventType.EV_WRITE;
					System.out.println("event:EV_WRITE");
				}
				if (key.isAcceptable()) {
					evSet = evSet | EventType.EV_ACCEPT;
					System.out.println("event:EV_ACCEPT");
				}

				// �¼�������
				attr.evHandle.handle(key.channel(), evSet, attr.obj);

				if ((attr.type & EventType.EV_PERSIST) == 0) {
					// �������EV_PERSIST���͵��¼�����ɾ��������key
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
		long timeOut;
		public Object obj;

		public EventAttr(int type, IEventHandle handle, Object obj, long timeOut) {
			this.type = type;
			this.obj = obj;
			this.timeOut = timeOut;
			this.evHandle = handle;
		}
	}
}
