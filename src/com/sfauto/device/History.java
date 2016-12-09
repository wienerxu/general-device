package com.sfauto.device;

import com.sfauto.device.history.Historian;

public class History {
	public static History history = null;
	
    public static synchronized History getInstance() {
        if (history == null) {
        	history = new History();
        }
        return history;
    }
    
    private History(){
    }
    
    public void save(long key,float value){
    	Historian.getInstance().save_trend(key, System.currentTimeMillis(), value);
    }
    
    public void save_event(EventData ed){
    	Historian.getInstance().save_alarm(ed);
    }    
    
    public HisData[] query(long key, long start, long end,long gap) {
    	return Historian.getInstance().query(key, start, end);
    }    
}
