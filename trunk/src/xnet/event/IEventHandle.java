package xnet.event;

import java.nio.channels.*;

/**
 * 事件处理器，用于事件触发时的回调
 * @author quanwei
 *
 */
public interface IEventHandle {
	/**
	 * 事件出发回调函数对象
	 * @param select	
	 * @param type	事件类型
	 * @param obj	附件
	 */
	void onIOReady(SelectableChannel select,int type,Object obj);
}
