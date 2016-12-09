package com.sfauto.device.builder;

import java.util.List;

import com.sfauto.device.DVariable;
import com.sfauto.device.Device;
import com.sfauto.device.DeviceProperty;
import com.sfauto.device.Variable;
import com.sfauto.realdb.DBTable;
import com.sfauto.realdb.JRDBSet;
import com.sfauto.realdb.record.RealDBDefine;
import com.sfauto.realdb.record.TableRecord;

public class DeviceModel extends javax.swing.table.AbstractTableModel {
	Device device = null;
	boolean isDirty = false;
	DevicePanel panel;
	List<DVariable> varList = null;
	
	static DBTable tableAnalog = JRDBSet.getInstance().getTable(RealDBDefine.tableID_SCADA_Analog);
	static DBTable tableDigit = JRDBSet.getInstance().getTable(RealDBDefine.tableID_SCADA_Digit);
	static DBTable tablePulse = JRDBSet.getInstance().getTable(RealDBDefine.tableID_SCADA_Pulse);
	
    public DeviceModel(DevicePanel panel,Device device) {
    	this.device = device;
    	this.panel = panel;
    	varList = device.getVarList();
    }

    public int getColumnCount() {
    	return 4;
    }

    public int getRowCount() {
    	if(varList == null){
    		return 0;
    	}else{
    		return varList.size();
    	}
    }

    public String getColumnName(int col) {
    	if(col == 0){
    		return "属性";
    	}else if(col == 1){
    		return "类型";
    	}else if(col == 2){
    		return "配置";
    	}else if(col == 3){
    		return "数值";
    	}
    	return null;
    }

    public Object getValueAt(int row, int col) {
        Object obj = null;
    	if(col == 0){
    		obj = varList.get(row).desc;
    	}else if(col == 1){
    		obj = DeviceProperty.translateType(varList.get(row).sample_type);
    	}else if(col == 2){    		
    		Variable var = varList.get(row).var;
    		obj = var.key;
    	}else if(col == 3){
    		DVariable dvar = varList.get(row);
    		Variable var = dvar.var;
    		if(var.type == DeviceProperty.PROPERTY_TYPE_REF){
    			DBTable table = null;
    			if(dvar.sample_type == DeviceProperty.SAMPLE_ANALOG){
    				table = tableAnalog;
    			}else if(dvar.sample_type == DeviceProperty.SAMPLE_DIGIT){
    				table = tableDigit;
    			}else if(dvar.sample_type == DeviceProperty.SAMPLE_PULSE){
    				table = tablePulse;
    			}
				if(table != null && var.key > 0){
					TableRecord record = table.findByKey(var.key);
					if(record != null){
						return record.getValue(dvar.field_index);
					}
				}    			
    		}else{
   				return var.getValue().toString();
    		}
    	}
        return obj;
    }

    public Class getColumnClass(int col) {
    	if(col == 0){
    		return String.class;
    	}else if(col == 1){
    		return String.class;
    	}else if(col == 2){
    		return Integer.class;
    	}else if(col == 3){
    		return String.class;
    	}
    	return null;
    }

    public boolean isCellEditable(int row, int col) {
    	if(col == 0 || col == 1){
    		return false;
    	}else if(col == 2){    		
    		Variable var = varList.get(row).var;
    		if(var.type != DeviceProperty.PROPERTY_TYPE_REF){
    			return false;
    		}
    	}else if(col == 3){
    		Variable var = varList.get(row).var;
    		if(var.type == DeviceProperty.PROPERTY_TYPE_REF){
    			return false;
    		}
    	}
    	return true;
    }

    public void setValueAt(Object value, int row, int col) {
    	if(col == 2){
    		DVariable dvar = varList.get(row);
    		Variable var = dvar.var;
    		int sample_type = dvar.sample_type;
    		
    		if(var.type == DeviceProperty.PROPERTY_TYPE_REF){
    			if(value != null){
    				if(value instanceof Integer){
		    			var.key = (int)value;
		    			isDirty = true;
		    			panel.notifySave(true);
		    			panel.table.revalidate();
		    			panel.table.repaint();
		    			panel.packColumn(panel.table, 2);		    			
    				}else{
	    				int[] keys = (int[])value;
	    				for(int i=0;i<keys.length;i++){
	    					if(row+i>=getRowCount()){
	    						break;
	    					}
	    					DVariable ddvar = varList.get(row+i);
	    		    		Variable var1 = ddvar.var;
	    		    		if(ddvar.sample_type == sample_type){
	    		    			var1.key = keys[i];
	    		    		}
	    				}
		    			isDirty = true;
		    			panel.notifySave(true);
		    			panel.table.revalidate();
		    			panel.table.repaint();
		    			panel.packColumn(panel.table, 2);
    				}
    			}
    		}
    	}else if(col == 3){
    		Variable var = varList.get(row).var;
    		if(var.type != DeviceProperty.PROPERTY_TYPE_REF){
    			if(var.type == DeviceProperty.PROPERTY_TYPE_INT){
    				var.setValue(Integer.parseInt(value.toString()));
    			}else if(var.type == DeviceProperty.PROPERTY_TYPE_FLOAT){
    				var.setValue(Float.parseFloat(value.toString()));
    			}else if(var.type == DeviceProperty.PROPERTY_TYPE_STRING){
    				var.setValue(value.toString());
    			}
    			isDirty = true;
    			panel.notifySave(true);
    		}
    	}
    }
}
