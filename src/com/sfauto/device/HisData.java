package com.sfauto.device;

public class HisData implements Comparable{
	public long time;
	public float value;
	
	public HisData(long time,float value){
		this.time = time;
		this.value = value;
	}
	
    public int compareTo(Object object) {
    	HisData ssi = (HisData)object;
        return (this.time<ssi.time ? -1 : (this.time==ssi.time ? 0 : 1));
    }

    public HisData clone(){
    	HisData ssi = new HisData(this.time,this.value);
        return ssi;
    }

    public void copy(HisData ssi){
        this.time = ssi.time;
        this.value = ssi.value;
    }
}
