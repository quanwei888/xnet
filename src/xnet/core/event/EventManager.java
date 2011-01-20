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
 * 事件管理器，用于异步IO操作
 * 
 * @author quanwei
 * 
 */
public class EventManager {
	static Log logger = LogFactory.getLog(EventManager.class);

	protected Selector selector;
	protected long timeout;

	/**
	 * 构造函数
	 * 
	 * @throws IOException
	 */
	public EventManager() throws IOException {
		selector = Selector.open();
		timeout = 0;
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
	 * @param timeout
	 *            超时时间
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
			// 取最小的超时设置
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
	 * 删除一个监听事件
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
	 * 监听，轮询监听注册的事件，直到事件集合中为空
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
					if (attr.timeout == 0) {
						continue;
					}

					if (timeCost > attr.timeout) {
						// 事件处理器
						attr.evHandle.onIOReady(key.channel(), EventType.EV_TIMEOUT, attr.obj);
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

				// 事件处理器
				attr.evHandle.onIOReady(key.channel(), evSet, attr.obj);
				if ((attr.type & EventType.EV_PERSIST) == 0) {
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
