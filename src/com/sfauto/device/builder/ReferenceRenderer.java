package com.sfauto.device.builder;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import com.sfauto.device.DeviceProperty;

public class ReferenceRenderer extends DefaultTableCellRenderer {
    private Color bkcolor;
    private Color fgcolor;
    private boolean isCrossRender = true;
    PointSelector pointSelector;
    int type,sample_type;
    
    public ReferenceRenderer(PointSelector pointSelector,Color bkcolor, Color fgcolor,
                                boolean isCrossRender) {
        this.bkcolor = bkcolor;
        this.fgcolor = fgcolor;
        this.pointSelector = pointSelector;
        this.isCrossRender = isCrossRender;
    }

    public void setValue(Object value) {
        if (value instanceof Integer) {
    		int key = (int)value;
    		String info = key + "";
    		if(type == DeviceProperty.PROPERTY_TYPE_REF){    			
            	info = pointSelector.getRender(sample_type, key);
            }else{
            	info = "";
            }
    		setText(info);
        } else {
            super.setValue(value);
        }
    }
    
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus, int row,
            int column) {
        if (isCrossRender) {
            if (row % 2 == 0) {
                setBackground(bkcolor);
            } else {
                setBackground(Color.WHITE);
            }
        } else {
            setBackground(bkcolor);
        }
    	if (value instanceof Integer) {
    		int key = (int)value;
	
    		type = ((DeviceModel)table.getModel()).varList.get(row).var.type;
	        sample_type =((DeviceModel)table.getModel()).varList.get(row).sample_type;
    		
    		if(key == 0){
        		setBackground(Color.orange);
        	}
    	}        
        setForeground(fgcolor);
        
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}
