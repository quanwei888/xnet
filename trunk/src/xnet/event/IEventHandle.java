package xnet.event;

import java.nio.channels.*;

/**
 * �¼��������������¼�����ʱ�Ļص�
 * @author quanwei
 *
 */
public interface IEventHandle {
	/**
	 * �ص�����
	 * @param select	
	 * @param type	�������¼�����
	 * @param obj	ע��ʱ�ĸ���
	 */
	void handle(SelectableChannel select,int type,Object obj);
}
