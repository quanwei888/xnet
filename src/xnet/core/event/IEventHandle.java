package xnet.core.event;

import java.nio.channels.*;

/**
 * �¼��������������¼�����ʱ�Ļص�
 * @author quanwei
 *
 */
public interface IEventHandle {
	/**
	 * �¼������ص���������
	 * @param select	
	 * @param type	�¼�����
	 * @param obj	����
	 */
	void onIOReady(SelectableChannel select,int type,Object obj);
}
