package com.sfauto.device.builder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

public class ReferenceEditor extends AbstractCellEditor implements
        TableCellEditor,SelectionEventListener,ActionListener {

    private Integer value;
    int type;
    PointSelector pointSelector;
    private JDialog dialog;
    private JComponent editorComponent;
    private int x, y;
    private int width, height;
    private int screen_width, screen_height;
    
    boolean isSelect = false;
    JFrame frame;
    
    public ReferenceEditor(JFrame frame, PointSelector pointSelector) {
        dialog = new JDialog(frame, "—°‘Ò¡ø≤‚µ„", false);
        this.frame = frame;
        //dialog.setUndecorated(true);
        dialog.setResizable(true);
        
        this.pointSelector = pointSelector;
        
        dialog.getContentPane().add(pointSelector);
        dialog.pack();
        
        Rectangle rect = frame.getBounds();
        screen_width = rect.width + rect.x;
        screen_height = rect.height;
        width = pointSelector.getBounds().width;
        height = pointSelector.getBounds().height;
        
        x = (screen_width - width) / 2;
        y = (screen_height - height) / 2;

        pointSelector.addSelectionListener(this);

        dialog.addWindowListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent e) {
            	isSelect = false;
            	dialog.setVisible(false);                
                fireEditingStopped();
            }
        });

        final JButton button = new JButton("");
        editorComponent = button;
        button.addActionListener(this);
        button.setBackground(Color.white);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    public void actionPerformed(ActionEvent ae) {
    	pointSelector.setValue(type,value);
        dialog.setVisible(true);
    }

    @Override
    public void selected(EventObject se) {
    	isSelect = true;
        dialog.setVisible(false);
        fireEditingStopped();
    }

	@Override
	public void cancel() {
		isSelect = false;
		dialog.setVisible(false);
		fireEditingStopped();
	}
	
    public void fireEditingStopped() {
        super.fireEditingStopped();
    }

    public Object getCellEditorValue() {
    	if(isSelect){
	        if (null != pointSelector.getValue()) {
	            int[] keys = pointSelector.getValue();
	            return keys;
	        }else{
	        	return null;
	        }
    	}else{
    		return null;
    	}
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean bIsSelected,
                                                 int iRow,
                                                 int iCol) {
        Rectangle rect = table.getCellRect(iRow,
                                           table.convertColumnIndexToView(iCol), true);
        Point point = rect.getLocation();
        SwingUtilities.convertPointToScreen(point, table);
        
        Rectangle rect1 = frame.getBounds();
        screen_width = rect1.width + rect1.x;
        screen_height = rect1.height;
        
        x = point.x;
        y = point.y + rect.height;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > screen_width - width) {
            x = screen_width - width;
        }
        if (y > screen_height - height) {
            y = screen_height - height;
        }
        dialog.setBounds(x, y, width, height);
        this.value = (Integer) value;
        this.type =((DeviceModel)table.getModel()).varList.get(iRow).sample_type;
        
        String sss = pointSelector.getRender(type, (int)value);        
        ((JButton) editorComponent).setText(sss);
        
        return editorComponent;
    }

    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            return ((MouseEvent) evt).getClickCount() >= 2;
        }
        return true;
    }
}
