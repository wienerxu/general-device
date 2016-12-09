package com.sfauto.device;

import com.sfauto.toolkits.threadpool.PooledThread;


public class DeviceTask extends PooledThread {
	String templateName,command,parameter;
	
    public DeviceTask(String templateName,String command,String parameter) {
    	this.templateName = templateName;
    	this.command = command;
    	this.parameter = parameter;
    }

    public void run() {
        running = true;
    	DeviceApp[] devices = DeviceFactory.getInstance().getDevices(templateName);
    	for(DeviceApp device:devices){
    		try {
				device.operate(command, parameter);
			} catch (Exception e) {
				continue;
			}
    	}
        running = false;
    }
	
}
