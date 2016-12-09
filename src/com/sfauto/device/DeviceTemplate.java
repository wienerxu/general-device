package com.sfauto.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sfauto.toolkits.task.TaskTrigger;

public class DeviceTemplate implements CommonTreeNodeObject{
	public String description;
	public String name;
	String class_name = null;
	String scheduleCommand,scheduleParameter,scheduleCycle;
	
	DeviceScript script = null;
	
	boolean hasRefProperty = false;
	boolean hasIntProperty = false;
	boolean hasFloatProperty = false;
	boolean hasStringProperty = false;
	
	boolean hasPlugin = false;
	
	Map<String,DeviceProperty> properties = new HashMap<String,DeviceProperty>();
	List<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();
	
	public void addProperty(DeviceProperty property){
		properties.put(property.name, property);
		propertyList.add(property);
	}
	
	//用于树形结构
	public boolean equals(Object obj){
		if(obj instanceof DeviceTemplate){
			DeviceTemplate template = (DeviceTemplate)obj;
			return this.name.equals(template.name);
		}else{
			return false;
		}
	}
	
	public String toString(){
		return name;
	}
	
	public String getPropertyUnit(String propertyName){
		if(properties.containsKey(propertyName)){
			return properties.get(propertyName).getUnit();
		}
		return "";
	}
	
	public String getPropertyDesc(String propertyName){
		if(properties.containsKey(propertyName)){
			return properties.get(propertyName).getDesc();
		}
		return "";
	}
	
	@Override
	public void destroy() {
	}
}
