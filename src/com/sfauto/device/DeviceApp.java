package com.sfauto.device;

import com.sfauto.realdb.record.TableRecord;

public interface DeviceApp {
	//���º������ڿ��ע�����
	public void setDevice(Device device);
	public void addChild(DeviceApp device);
	public void setParent(DeviceApp parent);
	public void setScript(DeviceScript script);

	//����Ϊͨ�÷���
	public Device getDevice();
	public Object get(String name);	
	public boolean set(String name,Object value);
	public void save(String name,Object value);
	public HisData[] query(String name,long start,long end,long gap);
	public DeviceApp[] getChildren();	
	public DeviceApp getParent();
	public TableRecord getRecord(String name);
	
	//���º�����Ӧ����Ҫʵ��
	public Result operate(String command,String parameter);	
}
