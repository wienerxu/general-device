package com.sfauto.device.builder;

import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.JPanel;
import java.util.EventObject;
/**
 * <p>Title: RtdbEditor</p>
 *
 * <p>Description: 实时库编辑器</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: 研究中心自动化室</p>
 *
 * @author wiener
 * @version 1.0
 */
public abstract class SelectablePanel extends JPanel {

    protected Hashtable listeners = new Hashtable();

    public void addSelectionListener(SelectionEventListener listener) {
        listeners.put(listener, listener);
    }

    public void removeSelectionListener(SelectionEventListener listener) {
        listeners.remove(listener);
    }

    public void fireSelectionEvent() {
        EventObject se = new EventObject(this);
        Enumeration enume = listeners.elements();
        while(enume.hasMoreElements()) {
            SelectionEventListener sel = (SelectionEventListener)enume.nextElement();
            sel.selected(se);
        }
    }
    
    public void fireCancelEvent(){
        EventObject se = new EventObject(this);
        Enumeration enume = listeners.elements();
        while(enume.hasMoreElements()) {
            SelectionEventListener sel = (SelectionEventListener)enume.nextElement();
            sel.cancel();
        }    	
    }
}
