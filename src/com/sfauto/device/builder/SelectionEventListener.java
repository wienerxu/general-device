package com.sfauto.device.builder;

import java.util.EventListener;
import java.util.EventObject;
/**
 * <p>Title: RtdbEditor</p>
 *
 * <p>Description: ʵʱ��༭��</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: �о������Զ�����</p>
 *
 * @author wiener
 * @version 1.0
 */
public interface SelectionEventListener extends EventListener {

    public void selected(EventObject se);
    
    public void cancel();

}
