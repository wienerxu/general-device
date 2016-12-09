package com.sfauto.device;

import java.util.ArrayList;
import java.util.List;

import com.sfauto.realdb.record.TableRecord;

public class CommonDevice implements DeviceApp{
	Device device = null;
	DeviceScript script = null;
	List<DeviceApp> childList = new ArrayList<DeviceApp>();
	DeviceApp parent = null;
	
	@Override
	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
	public Result operate(String command,String parameter) {
		if(script != null){
			return script.runScript(this, command, parameter);	
		}else{		
			Result result = new Result();
			result.isSuccess = false;
			result.info = "common device is not operatable!";
			return result;
		}
	}

	@Override
	public Object get(String name) {
		if(device != null){
			return device.getProperty(name);
		}
		return null;
	}

	@Override
	public boolean set(String name, Object value) {
		if(device != null){
			return device.setProperty(name,value);
		}
		return false;
	}

	@Override
	public void save(String name,Object value) {
		if(device != null){
			device.save(name,value);
		}
	}
	
	public void save(){
		if(device != null){
			device.save();
		}
	}
	
	public void alarm(int status,int type,int level,String propName,String content){
		if(device != null){
			device.alarm(status, type, level, propName, content);
		}
	}
	
	@Override
	public void setScript(DeviceScript script) {
		this.script = script;		
	}

	@Override
	public TableRecord getRecord(String name) {
		if(device != null){
			return device.getPropertyRecord(name);
		}
		return null;
	}

	@Override
	public void addChild(DeviceApp device) {
		childList.add(device);		
	}

	@Override
	public DeviceApp[] getChildren() {
		DeviceApp[] children = new DeviceApp[childList.size()];
		childList.toArray(children);
		return children;
	}

	@Override
	public Device getDevice() {
		return device;
	}

	@Override
	public HisData[] query(String name, long start, long end, long gap) {
		if(device != null){
			long key = device.getKey(name);
			if(key > 0){
				return History.getInstance().query(key,start,end,gap);
			}
		}
		return null;
	}

	@Override
	public void setParent(DeviceApp parent) {
		this.parent = parent;		
	}

	@Override
	public DeviceApp getParent() {
		return parent;
	}

	public DeviceScript getScript(){
		return script;
	}
	

}
