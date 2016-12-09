package com.sfauto.device;

import com.sfauto.realdb.record.TableRecord;

public interface DeviceApp {
	//以下函数用于框架注入对象
	public void setDevice(Device device);
	public void addChild(DeviceApp device);
	public void setParent(DeviceApp parent);
	public void setScript(DeviceScript script);

	//以下为通用方法
	public Device getDevice();
	public Object get(String name);	
	public boolean set(String name,Object value);
	public void save(String name,Object value);
	public HisData[] query(String name,long start,long end,long gap);
	public DeviceApp[] getChildren();	
	public DeviceApp getParent();
	public TableRecord getRecord(String name);
	
	//以下函数各应用需要实现
	public Result operate(String command,String parameter);	
}
