package xnet.event;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * 事件管理器，用于异步IO操作
 * 
 * @author quanwei
 * 
 */
public class EventManager {
	protected Selector selector;
	protected long timeOut;

	/**
	 * 构造函数
	 * 
	 * @throws IOException
	 */
	public EventManager() throws IOException {
		selector = Selector.open();
		timeOut = 0;
	}

	/**
	 * 注册一个监听事件
	 * 
	 * @param channel
	 *            要监听的channel
	 * @param type
	 *            事件类型
	 * @param evHandle
	 *            监听对象，用于事件触发时回调
	 * @param obj
	 *            附件
	 * @param timeOut
	 *            超时时间
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
			// 取最小的超时设置
			this.timeOut = timeOut;
		}

		channel.register(selector, evSet, attr);
	}

	/**
	 * 删除一个监听事件
	 * 
	 * @param channel
	 */
	public void delEvent(SelectableChannel channel) {
		channel.keyFor(selector).cancel();
		System.out.println("cancel key");
	}

	/**
	 * 监听，轮询监听注册的事件，直到事件集合中为空
	 * 
	 * @throws IOException
	 */
	public void loop() throws IOException {
		while (true) {
			long stime = System.currentTimeMillis();
			int ret = selector.select();
			System.out.println("select return:" + ret);
			if (selector.keys().size() == 0) {
				// 没有事件被监听则结束循环
				break;
			}

			if (ret == 0) {
				// 超时处理
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
						// 事件处理器
						attr.evHandle.handle(key.channel(),
								EventType.EV_TIMEOUT, attr.obj);
						if ((attr.type & EventType.EV_PERSIST) == 0) {
							// 如果不是EV_PERSIST类型的事件，则删除关联的key
							key.cancel();
						}
					}
				}
				continue;
			}

			// 不是超时
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

				// 事件处理器
				attr.evHandle.handle(key.channel(), evSet, attr.obj);

				if ((attr.type & EventType.EV_PERSIST) == 0) {
					// 如果不是EV_PERSIST类型的事件，则删除关联的key
					key.cancel();
				}
			} 
		}
	}

	/**
	 * 事件属性，封装了超时、事件类型、附件
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
