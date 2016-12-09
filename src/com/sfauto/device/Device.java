package com.sfauto.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sfauto.realdb.DBTable;
import com.sfauto.realdb.JRDBSet;
import com.sfauto.realdb.record.TableRecord;

public class Device implements CommonTreeNodeObject{
	public static String INTERNAL_PROPERTY_PREFIX = "my_";
	
	int id;
	String name;
	String template;
	
	public Map<String,Variable> fields = new HashMap<String,Variable>(); 
	
	public Device(int id,String name,String template){
		this.id = id;
		this.name = name;
		this.template = template;
	}
	
	public String toString(){
		return name;
	}
	
	public String getType(){
		return template;
	}
	
	public int getID(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public Object getProperty(String name){
		if(isInternalProperty(name)){
			if(name.compareToIgnoreCase("my_name")==0){
				return name;
			}
			return null;
		}else{
			if(fields.containsKey(name)){
				return fields.get(name).getValue();
			}
		}
		return null;
	}
	
	public long getPropertyTime(String name){
		if(isInternalProperty(name)){
			if(name.compareToIgnoreCase("my_name")==0){
				return 0;
			}
		}else{
			if(fields.containsKey(name)){
				return fields.get(name).getTime();
			}
		}
		return 0;
	}
	
	public void save(String name,Object value){
		if(isInternalProperty(name)){
			return;
		}
		if(fields.containsKey(name)){
			if(value != null && (value instanceof Integer || value instanceof Float)){
				if(value instanceof Integer){
					History.getInstance().save(fields.get(name).lkey,(Integer)value);
				}else{
					History.getInstance().save(fields.get(name).lkey,(Float)value);
				}
			}
		}
	}
	
	public void save(){
		Iterator iter = fields.entrySet().iterator();
		while(iter.hasNext()){
			Entry entry = (Entry)iter.next();
			Variable var = (Variable)entry.getValue();
			Object value = var.getValue();
			if(value != null){
				if(value instanceof Integer){
					History.getInstance().save(var.lkey,(Integer)value);
				}else if(value instanceof Float){
					History.getInstance().save(var.lkey,(Float)value);
				}
			}
		}
	}
	
	public long getKey(String name){
		if(isInternalProperty(name)){
			return 0;
		}
		if(fields.containsKey(name)){
			return fields.get(name).lkey;
		}
		return 0;
	}	
	
	public TableRecord getPropertyRecord(String name){
		if(isInternalProperty(name)){
			return null;
		}else{
			if(fields.containsKey(name)){
				return fields.get(name).getRecord();
			}
		}
		return null;
	}
	
	public boolean setProperty(String name,Object value){
		if(!isInternalProperty(name)){
			if(fields.containsKey(name)){
				return fields.get(name).setValue(value);
			}
		}
		return false;
	}
	
	
	/**
	 * 内置属性，是否要开放给应用
	 * @param name
	 * @return
	 */
	boolean isInternalProperty(String name){
		if(name != null && name.length() != 0 && name.startsWith(INTERNAL_PROPERTY_PREFIX)){
			return true;
		}
		return false;
	}
	
	public boolean addProperty(String name,DeviceProperty property,int key){
		if(fields.containsKey(name)){
			return false;
		}
		if(property.isReference){
			DBTable table = JRDBSet.getInstance().getTable(property.getDB(), property.getTable());
			if(table == null){
				return false;
			}
			TableRecord record = table.findByKey(key);
			if(record == null){
				return false;
			}
			if(table.fieldIndexMap.size()==0){
				table.mapFieldIndex(TableRecord.name_mode);
			}
			int field_index = table.getFieldIndex(property.getField());
			long lkey = (table.getTableid()<<34)+record.key();
			Variable v = new Variable(lkey,record.key(),record,field_index,property.isReference);
			fields.put(name, v);
		}else{
			Variable v = new Variable(property.type,key);
			fields.put(name, v);
		}
		return true;
	}	

	public void alarm(int status,int type,int level,String propName,String content){
		EventData ed = new EventData();
		ed.dev_id = id;
		ed.status = status;
		ed.type = type;
		ed.level = level;
		ed.ltime = System.currentTimeMillis();
		ed.prop_name = propName;
		ed.content = content;
		History.getInstance().save_event(ed);
	}

	public List<NamedObject> getData(String catalog,boolean isNameMode){
		List<NamedObject> values = new ArrayList<NamedObject>();
		
		DeviceTemplate t = DeviceFactory.getInstance().models.getTemplate(template);
		if(t != null){
			boolean isAll = false;
			if(catalog == null || catalog.isEmpty()){
				isAll = true;
			}			
			for(DeviceProperty property:t.propertyList){
				if((isAll || property.catalog.equals(catalog)) && fields.containsKey(property.name)){
					if(isNameMode){
						values.add(new NamedObject(property.name, getProperty(property.name)));
					}else{
						values.add(new NamedObject(property.desc, getProperty(property.name)));
					}
				}	
			}
		}		
		return values;		
	}
	public List<NamedObjectEx> getDataEx(String catalog,boolean isNameMode){
		List<NamedObjectEx> values = new ArrayList<NamedObjectEx>();
		
		DeviceTemplate t = DeviceFactory.getInstance().models.getTemplate(template);
		if(t != null){
			boolean isAll = false;

			if(catalog == null || catalog.isEmpty()){
				isAll = true;
			}			
			for(DeviceProperty property:t.propertyList){
				if((isAll || property.catalog.equals(catalog)) && fields.containsKey(property.name)){
					if(isNameMode){
						values.add(new NamedObjectEx(property.name, getProperty(property.name),getPropertyTime(property.name)));
					}else{
						values.add(new NamedObjectEx(property.desc, getProperty(property.name),getPropertyTime(property.name)));
					}
				}	
			}
		}		
		return values;		
	}
	
	public Map<String,Object> getData2(String catalog,boolean isNameMode){
		Map<String,Object> values = new HashMap<String,Object>();
		
		DeviceTemplate t = DeviceFactory.getInstance().models.getTemplate(template);
		if(t != null){
			boolean isAll = false;
			if(catalog == null || catalog.isEmpty()){
				isAll = true;
			}
			List<Object> vars = new ArrayList<Object>();
			List<String> names = new ArrayList<String>();
			values.put("name", names);
			values.put("value", vars);
			
			for(DeviceProperty property:t.propertyList){
				if((isAll || property.catalog.equals(catalog)) && fields.containsKey(property.name)){
					if(isNameMode){
						names.add(property.name);
					}else{
						names.add(property.desc);						
					}
					vars.add(getProperty(property.name));
				}	
			}			
		}		
		return values;		
	}	
	
	//创建设备时使用
	public void initialize(){
		DeviceTemplate t = DeviceFactory.getInstance().getModels().getTemplate(template);
		if(t != null){
			for(DeviceProperty prop:t.propertyList){
				addPropertyEdit(prop.name, prop.desc,prop, 0);
			}			
		}
	}
	
	public boolean isAllSet(){
		Iterator iter = fields.entrySet().iterator();
		while(iter.hasNext()){
			Entry entry = (Entry)iter.next();
			Variable var = (Variable)entry.getValue();
			if(var.key == 0){
				return false;
			}
		}
		return true;
	}
	
	public boolean addPropertyEdit(String name,String desc,DeviceProperty property,int key){
		if(fields.containsKey(name)){
			return false;
		}
		Variable v = new Variable(property.type,key);
		fields.put(name, v);
		return true;
	}	
	
	public void setKey(String name,int key){
		if(fields.containsKey(name)){
			Variable var = fields.get(name);
			var.key = key;
		}
	}

	@Override
	public void destroy() {
		DeviceFactory.getInstance().modelDB.removeDevice(this);
	}
	
	public List<DVariable> getVarList(){
		List<DVariable> variables = new ArrayList<DVariable>();		
		DeviceTemplate t = DeviceFactory.getInstance().models.getTemplate(template);
		if(t != null){
			for(DeviceProperty prop:t.propertyList){
				Variable var = fields.get(prop.name);
				int type = prop.getType();
				int field_index = -1;
				
				if(prop.isReference){
					DBTable table = JRDBSet.getInstance().getTable(prop.getDB(), prop.getTable());
					if(table != null){
						if(table.fieldIndexMap.size()==0){
							table.mapFieldIndex(TableRecord.name_mode);
						}
						field_index = table.getFieldIndex(prop.getField());
					}
				}
				variables.add(new DVariable(prop.name,prop.desc,var,type,field_index));	
			}
		}
		return variables;		
	}	
	
	public Device clone(){
		Device newDevice = new Device(-1,this.name,this.template);
		
		Iterator iter = fields.entrySet().iterator();
		while(iter.hasNext()){
			Entry entry = (Entry)iter.next();
			String name = (String)entry.getKey();
			Variable var = (Variable)entry.getValue();
			
			newDevice.fields.put(name, var.clone());
		}
		
		return newDevice;
	}
}


