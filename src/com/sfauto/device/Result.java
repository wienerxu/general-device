package com.sfauto.device;

import java.util.HashMap;
import java.util.Map;

public class Result {
	public boolean isSuccess = false;
	public String info = "";
	public Map<String,Object> values = null;
	
	public Result(){	
		values = new HashMap<String,Object>();
	}
	
	public Result(boolean isSuccess,String info){
		this.isSuccess = isSuccess;
		this.info = info;
		values = new HashMap<String,Object>();
	}
	
	public void put(String key,Object value){
		values.put(key, value);
	}
}
