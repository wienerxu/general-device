package com.sfauto.device;

public class NamedObjectEx {
	String name;
	Object value;
	long time;
	
	public NamedObjectEx(String name,Object value,long time){
		this.name = name;
		this.value = value;		
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
}
