package com.sfauto.device;

public class DeviceProperty {
	public static int PROPERTY_TYPE_UNDEFINED = 0;
	public static int PROPERTY_TYPE_REF = 1;   //ϸ��Ϊ�ɼ���
	public static int PROPERTY_TYPE_INT = 2;
	public static int PROPERTY_TYPE_FLOAT = 3;
	public static int PROPERTY_TYPE_STRING = 4;
	
	public final static int SAMPLE_ANALOG = 100;
	public final static int SAMPLE_DIGIT = 101;
	public final static int SAMPLE_PULSE = 102;
	
	String name;
	String desc;
	int type = 0;
	String db,table,field;
	String defaultValue, unit;
	
	String catalog;
	
	boolean isReference = false;
	
	public String getDB(){
		if(type == PROPERTY_TYPE_REF){
			return db;
		}else{
			return "";
		}
	}
	
	public String getTable(){
		if(type == PROPERTY_TYPE_REF){
			return table;
		}else{
			return "";
		}		
	}
	
	public String getField(){
		if(type == PROPERTY_TYPE_REF){
			return field;
		}else{
			return "";
		}		
	}
	
	public String getUnit(){
		return unit;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public int getType(){
		if(type == PROPERTY_TYPE_REF){
			if(getTable().equals("Analog")){
				return SAMPLE_ANALOG;
			}else if(getTable().equals("Digit")){
				return SAMPLE_DIGIT;
			}else if(getTable().equals("Pulse")){
				return SAMPLE_PULSE;
			}
		}else{
			return type;
		}
		return 0;
	}
	
	public static String translateType(int t){
		if(t == SAMPLE_ANALOG){
			return "�ɼ�ģ����";
		}else if(t == SAMPLE_DIGIT){
			return "�ɼ�������";
		}else if(t == SAMPLE_PULSE){
			return "�ɼ������";
		}else if(t == PROPERTY_TYPE_INT){
			return "������";
		}else if(t == PROPERTY_TYPE_FLOAT){
			return "������";
		}else if(t == PROPERTY_TYPE_STRING){
			return "�ַ���";
		}
		return "δ��������";
	}
}
