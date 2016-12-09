package com.sfauto.device;


/**
 * 该类仅在编辑态使用
 * @author xuyanming
 *
 */
public class DVariable {
	public DVariable(String name,String desc,Variable var,int type,int field_index){
		this.name = name;
		this.desc = desc;
		this.var = var;
		this.sample_type = type;
		this.field_index = field_index;
	}
	
	public String name;
	public String desc;
	public Variable var;
	public int sample_type;
	public int field_index;
	
}
