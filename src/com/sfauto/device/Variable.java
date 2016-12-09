package com.sfauto.device;

import com.sfauto.realdb.record.Record_SCADA_Analog;
import com.sfauto.realdb.record.Record_SCADA_Digit;
import com.sfauto.realdb.record.Record_SCADA_Pulse;
import com.sfauto.realdb.record.TableRecord;

public class Variable {
	int field_index;
	public TableRecord record = null;
	boolean isReference = false;
	
	public int type,key;
	long lkey;
	
	public Variable(long lkey,int key,TableRecord record,int field_index,boolean isReference){
		this.record = record;
		this.field_index = field_index;
		this.isReference = isReference;
		this.key = key;
		this.lkey = lkey;
	}
	
	public Variable(int type,int key){
		this.key = key;
		this.type = type;
		lkey = (type << 32) + key;
	}
	
	public Variable clone(){
		Variable newVar = null;
		if(isReference){
			newVar = new Variable(this.lkey,this.key,this.record,this.field_index,true);
		}else{
			newVar = new Variable(this.type,this.key);
		}
		return newVar;
	}
	
	public Object getValue(){
		if(isReference){
			return record.getValue(field_index);
		}else{
			if(type == DeviceProperty.PROPERTY_TYPE_INT){
				return PropertyCache.getInstance().getInt(key);
			}else if(type == DeviceProperty.PROPERTY_TYPE_FLOAT){
				return PropertyCache.getInstance().getFloat(key);
			}else if(type == DeviceProperty.PROPERTY_TYPE_STRING){
				return PropertyCache.getInstance().getString(key);
			}else{
				return null;
			}
		}
	}
	
	public long getTime(){
		long time = 0;
		if(isReference){
			if(record instanceof Record_SCADA_Analog){
				time = ((Record_SCADA_Analog)record).get_tSnapTime().getTime();
			}else if(record instanceof Record_SCADA_Digit){
				time = ((Record_SCADA_Digit)record).get_tSnapTime().getTime();
			}else if(record instanceof Record_SCADA_Pulse){
				time = ((Record_SCADA_Pulse)record).get_tSnapTime().getTime();
			}			
		}else{
			if(type == DeviceProperty.PROPERTY_TYPE_INT){
				return PropertyCache.getInstance().getIntTime(key);
			}else if(type == DeviceProperty.PROPERTY_TYPE_FLOAT){
				return PropertyCache.getInstance().getFloatTime(key);
			}else if(type == DeviceProperty.PROPERTY_TYPE_STRING){
				return PropertyCache.getInstance().getStringTime(key);
			}else{
				return 0;
			}
		}
		return time;
	}
	
	public boolean setValue(Object value){
		if(isReference){
			return false;
		}
		if(type == DeviceProperty.PROPERTY_TYPE_INT){
			if(value instanceof Integer){
				PropertyCache.getInstance().putInt(key,(Integer)value);
				return true;
			}
		}else if(type == DeviceProperty.PROPERTY_TYPE_FLOAT){
			if(value instanceof Float){
				PropertyCache.getInstance().putFloat(key,(Float)value);
				return true;
			}
		}else if(type == DeviceProperty.PROPERTY_TYPE_STRING){
			if(value instanceof String){
				PropertyCache.getInstance().putString(key,(String)value);
				return true;
			}		
		}
		return false;
	}
	
	TableRecord getRecord(){
		if(!isReference){
			return null;
		}
		return record;
	}
}
