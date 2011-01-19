package xnet.event;

import java.nio.channels.*;

/**
 * 事件处理器，用于事件触发时的回调
 * @author quanwei
 *
 */
public interface IEventHandle {
	/**
	 * 回调方法
	 * @param select	
	 * @param type	触发的事件类型
	 * @param obj	注册时的附件
	 */
	void handle(SelectableChannel select,int type,Object obj);
}
