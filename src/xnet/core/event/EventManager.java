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
 * 事件管理器，用于异步IO操作
 * 
 * @author quanwei
 * 
 */
public class EventManager {
	static Log logger = LogFactory.getLog(EventManager.class);

	/**
	 * Selector，每个EventManager中唯一，线程不安全
	 */
	protected Selector selector;
	/**
	 * key集合中最小的超时设置
	 */
	protected long minTimeOut;
	/**
	 * 待注册的key集合
	 */
	protected Set<EventAttr> addSet = new HashSet<EventAttr>();
	protected Set<EventAttr> timeoutSet = new HashSet<EventAttr>();

	/**
	 * 构造函数
	 * 
	 * @throws IOException
	 */
	public EventManager() throws IOException {
		selector = Selector.open();
		minTimeOut = 0;
	}

	/**
	 * 注册一个监听事件
	 * 
	 * @param socket
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
	public void addEvent(SelectableChannel socket, int type, IEventHandle evHandle, Object obj, long timeout) {
		logger.debug("add event:" + socket);
		EventAttr attr = new EventManager.EventAttr(type, evHandle, timeout, obj, socket);
		addSet.add(attr);
		if (timeout > 0) {
			// 加入到超时集合中，用于超时判断
			timeoutSet.add(attr);
		}
	}

	/**
	 * 删除一个监听事件
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
	 * 更新keys，删除cancel状态的key，重新设置要增加的key
	 * 
	 * @throws IOException
	 */
	protected void updateKeys() throws IOException {
		// 删除cancel状态的key
		selector.selectNow();
		// 重新设置要增加的key
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
				// 取最小的超时设置
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
	 * 监听，轮询监听注册的事件，直到事件集合中为空
	 * 
	 * @throws IOException
	 */
	public void loop() {
		while (true) {
			try {
				updateKeys();
				if (selector.keys().size() == 0) {
					// 没有事件被监听则结束循环
					break;
				}
				long stime = System.currentTimeMillis();
				int ret = selector.select(minTimeOut);
				long timeCost = System.currentTimeMillis() - stime;
				logger.debug("select return:" + ret);

				// IO事件处理
				Iterator<SelectionKey> eventIter = selector.selectedKeys().iterator();
				while (eventIter.hasNext()) {
					SelectionKey key = eventIter.next();
					eventIter.remove();
					if (!key.isValid()) {
						key.cancel();
						continue;
					}					 
					
					EventAttr attr = (EventAttr) key.attachment();
					timeoutSet.remove(attr);// 从超时key集合中移除
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
					// 回调函数
					attr.evHandle.onIOEvent(key.channel(), evSet, attr.obj);
				}

				// 超时事件处理
				Iterator<EventAttr> timeEventIter = timeoutSet.iterator();
				while (timeEventIter.hasNext()) {
					EventAttr attr = timeEventIter.next();
					if (timeCost >= attr.timeout) {
						timeEventIter.remove();
						attr.evHandle.onIOEvent(attr.socket, EventType.EV_TIMEOUT, attr.obj);
						//取消该事件
						attr.socket.keyFor(selector).cancel();
					}
				}
			} catch (IOException e) {
				logger.warn(e.getStackTrace());
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
