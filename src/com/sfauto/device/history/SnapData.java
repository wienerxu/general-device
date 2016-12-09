package com.sfauto.device.history;

public class SnapData implements Comparable{
    public long time;
    public float value;

    public SnapData(){
    }

    public SnapData(long time,float value) {
        this.time = time;
        this.value = value;
    }

    public int compareTo(Object object) {
        SnapData ssi = (SnapData)object;
        return (this.time<ssi.time ? -1 : (this.time==ssi.time ? 0 : 1));
    }

    public SnapData clone(){
        SnapData ssi = new SnapData(this.time,this.value);
        return ssi;
    }

    public void copy(SnapData ssi){
        this.time = ssi.time;
        this.value = ssi.value;
    }
}
