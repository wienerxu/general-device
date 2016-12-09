package com.sfauto.device;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class DeviceModels {
    public CommonTree<DeviceTemplate> models = new CommonTree<DeviceTemplate>();  
	Map<String,DeviceTemplate> templateMap = new HashMap<String,DeviceTemplate>();
	Map<Integer,DeviceApp> idedMap = new HashMap<Integer,DeviceApp>();
	Map<String,List<DeviceApp>> templatedMap = new HashMap<String,List<DeviceApp>>();
	
	//仅在编辑模式下
	public CommonTree<Device> deviceTree = null;
    
    String path = null;
    
	public DeviceModels(){
	}
	
    void loadModel(String path){
    	this.path = path;
    	CommonTreeNode root = models.getRoot();  
    	
    	loadDeviceTemplate(root,"");  
    }
    
    public DeviceTemplate getTemplate(String name){
    	return templateMap.get(name);
    }
    
    //此函数递归，需要建模型时避免回环
    void loadDeviceTemplate(CommonTreeNode node,String tname){
    	String modelPath = path  + "/model";
        File dir = new File(modelPath);
        if (dir.exists() && dir.isDirectory()) {
        	File[] files = dir.listFiles();
        	for(File file:files){
        		String file_name = file.getName();
        		//file_name = file_name.toLowerCase();
        		if(file_name.endsWith(".xml")){
		            try {
		                String item = null;
		
		                SAXBuilder builder = new SAXBuilder();
		                Document doc = builder.build(file);
		                Element root = doc.getRootElement();
		                
		                String name = root.getAttributeValue("name");
		                String desc = root.getAttributeValue("desc");
		                String parent = root.getAttributeValue("parent");
		                String scheduleFunction = root.getAttributeValue("scheduleFunction");
		                String scheduleParameter = root.getAttributeValue("scheduleParameter");
		                String schedule = root.getAttributeValue("schedule");
		                
		                if(parent == null || parent.isEmpty()){
		                	parent = "";
		                }

		                if(parent.indexOf(",") > 0){
		                	String[] tt = parent.split(",");
		                	boolean isFound = false;
		                	for(String t:tt){
				                if(tname.equals(t)){
				                	isFound = true;
				                }		                		
		                	}
		                	if(!isFound){
		                		continue;
		                	}
		                }else{
			                if(!tname.equals(parent)){
			                	continue;
			                }
		                }
		                
		                String deviceName = file_name.substring(0, file_name.length()-4);
		                
		                DeviceTemplate template = new DeviceTemplate();
		                template.name = name;
		                template.description = desc;
		                template.scheduleCycle = schedule;
		                template.scheduleCommand = scheduleFunction;
		                template.scheduleParameter = scheduleParameter;
		                //templates.put(name, template);
		                
		                File scriptFile = new File(path+"/js",deviceName+".js");
		                if(scriptFile.exists()){
		                	DeviceScript script = new DeviceScript(scriptFile);
		                	template.script = script;
		                }
		                
		                List properties = root.getChildren();
		                
		                if(properties != null){
		    				Iterator i = properties.iterator();
		    				while (i.hasNext()) {
		    					Element e_property = (Element) i.next();
			                    String type = e_property.getAttributeValue("type");
			                    desc = e_property.getAttributeValue("desc");
			                    name = e_property.getAttributeValue("name");
			                    String catalog = e_property.getAttributeValue("catalog");
			                    String unit = e_property.getAttributeValue("UNIT");
			                    		
			                    DeviceProperty property = new DeviceProperty();
			                    property.name = name;
			                    property.desc = desc;
			                    property.catalog = catalog;
			                    property.unit = unit;
			                    
			                    if(type.compareToIgnoreCase("point") == 0){
				                    property.type = property.PROPERTY_TYPE_REF;
			                    	String db = e_property.getAttributeValue("RDDB");
			                    	String table = e_property.getAttributeValue("RDTable");
			                    	String field = e_property.getAttributeValue("RDField");
			                    	
			                    	//String display = e_property.getAttributeValue("Display");
			                    	property.db = db;
			                    	property.table = table;
			                    	property.field = field;
			                    	property.isReference = true;
			                    	//property.display = display;
			                    	template.hasRefProperty = true;
			                    }else if(type.compareToIgnoreCase("int") == 0){
			                    	property.type = property.PROPERTY_TYPE_INT;
			                    	property.defaultValue = e_property.getAttributeValue("RdefValue");
			                    	template.hasIntProperty = true;
			                    }else if(type.compareToIgnoreCase("float") == 0){
			                    	property.type = property.PROPERTY_TYPE_FLOAT;
			                    	property.defaultValue = e_property.getAttributeValue("RdefValue");
			                    	template.hasFloatProperty = true;
			                    }else if(type.compareToIgnoreCase("string") == 0){
			                    	property.type = property.PROPERTY_TYPE_STRING;
			                    	property.defaultValue = e_property.getAttributeValue("RdefValue");
			                    	template.hasStringProperty = true;
			                    }else{
			                    	continue;
			                    }
			                    template.addProperty(property);
			                }
		                }		    
		                CommonTreeNode n = node.addChild(template);
		                templateMap.put(template.name,template);		                
		                loadDeviceTemplate(n,template.name);
		            } catch (Exception e) {
		            	e.printStackTrace();
		            }
        		}
        	}
    	}
    }
    
    Map<String,Integer> loadProperties(Connection conn,int id,boolean hasRefProperty,boolean hasIntProperty,
    		boolean hasFloatProperty,boolean hasStringProperty){
    	if(conn == null){
    		return null;
    	}
    	Map<String,Integer> properties = new HashMap<String,Integer>();
    	
    	if(conn != null){  	
    		if(hasIntProperty){
	    		try {
	    			Statement statement = conn.createStatement();
					ResultSet rs = statement.executeQuery("SELECT ID,name,value FROM dev_int WHERE dev_id="+id);
					if (rs != null) {
						int property_id,value;
						String prop_name;
						while (rs.next()) {
							property_id = rs.getInt(1);
							prop_name = rs.getString(2);
							value = rs.getInt(3);
							properties.put(prop_name, property_id);
							PropertyCache.getInstance().putInt(property_id, value);
						}
						rs.close();
						rs = null;
					}
					statement.close();
				} catch (SQLException e) {
				}
			}
    		if(hasFloatProperty){
	    		try {
	    			Statement statement = conn.createStatement();
					ResultSet rs = statement.executeQuery("SELECT ID,name,value FROM dev_float WHERE dev_id="+id);
					if (rs != null) {
						int property_id;
						float value;
						String prop_name;
						while (rs.next()) {
							property_id = rs.getInt(1);
							prop_name = rs.getString(2);
							value = rs.getFloat(3);
							properties.put(prop_name, property_id);
							PropertyCache.getInstance().putFloat(property_id, value);
						}
						rs.close();
						rs = null;
					}
					statement.close();
				} catch (SQLException e) {
				}
			}    		
    		if(hasStringProperty){
	    		try {
	    			Statement statement = conn.createStatement();
					ResultSet rs = statement.executeQuery("SELECT ID,name,value FROM dev_string WHERE dev_id="+id);
					if (rs != null) {
						int property_id;
						String prop_name,value;
						while (rs.next()) {
							property_id = rs.getInt(1);
							prop_name = rs.getString(2);
							value = rs.getString(3);
							properties.put(prop_name, property_id);
							PropertyCache.getInstance().putString(property_id, value);
						}				
						rs.close();
						rs = null;
					}
					statement.close();
				} catch (SQLException e) {
				}
			}
    		if(hasRefProperty){
	    		try {
	    			Statement statement = conn.createStatement();
					ResultSet rs = statement.executeQuery("SELECT name,record_id FROM dev_reference WHERE dev_id="+id);
					if (rs != null) {
						int record_id;
						String prop_name;
						while (rs.next()) {
							prop_name = rs.getString(1);
							record_id = rs.getInt(2);
							properties.put(prop_name, record_id);
						}				
						rs.close();
						rs = null;
					}
					statement.close();
				} catch (SQLException e) {
				}
			}    		
    	}    	
    	return properties;
    }
    
    public boolean loadDevices(Connection conn,boolean isRunMode){
    	if(conn == null){
    		return false;
    	}
    	if(isRunMode){
    		return loadRunDevice(conn);
    	}else{
    		return loadModelDevice(conn);
    	}
    }
    
    public boolean loadRunDevice(Connection conn){
    	Map<Integer,List<Integer>> pidMap = new HashMap<Integer,List<Integer>>();   	
    	try {
    		Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT ID,name,PID,template FROM device ORDER BY ID");
			if (rs != null) {
				int id, pid;
				String dev_name,dev_temp;
				while (rs.next()) {
					id = rs.getInt(1);
					dev_name = rs.getString(2);
					pid = rs.getInt(3);
					dev_temp = rs.getString(4);
					
	        		DeviceTemplate template = getTemplate(dev_temp);
	        		
	        		if(template == null){
	        			continue;
	        		}
	        		
	        		if(pid > 0){
	        			List<Integer> ids = pidMap.get(pid);
	        			if(ids == null){
	        				ids = new ArrayList<Integer>();
	        				pidMap.put(pid, ids);
	        			}
        				ids.add(id);
	        		}
	        		
	        		Device device = new Device(id,dev_name,dev_temp);
	        		
	        		//load propertys from device tables.
	        		Map<String,Integer> infos = loadProperties(conn,id,template.hasRefProperty,
	        				template.hasIntProperty,template.hasFloatProperty,template.hasStringProperty);
	        		
	        		if(infos == null){
	        			continue;
	        		}
	        		
	    			Iterator iterator = infos.entrySet().iterator();
	    			while(iterator.hasNext()){
	    				Entry entry = (Entry)iterator.next();
	    				String name = (String)entry.getKey();
	    				Integer key = (Integer)entry.getValue();
	    				if(template.properties.containsKey(name)){
	    					device.addProperty(name, template.properties.get(name), key);
	    				}
	    			}
	    			
	    			DeviceApp app = null;
	    			if(template.hasPlugin){
	    				boolean isOk = false;
	    				try {
							Class cls_name = Class.forName(template.class_name);
							app = (DeviceApp) cls_name.newInstance();
							isOk = true;
	    				}catch(Exception e){
	    				}
	    				if(!isOk){
	    					app = new CommonDevice();
	    				}
	    			}else{
	    				app = new CommonDevice();
	    			}
	    			
	    			if(app != null){
						if(template.script != null){
							app.setScript(template.script);
						}
						app.setDevice(device);
						idedMap.put(id,app);
						//namedMap.put(dev_name,app);
						
						List<DeviceApp> devs = null;
						if(templatedMap.containsKey(dev_temp)){
							devs = templatedMap.get(dev_temp); 
						}else{
							devs = new ArrayList<DeviceApp>();
							templatedMap.put(dev_temp,devs);
						}
						devs.add(app);
	    			}   						
				}
				rs.close();
				rs = null;
			}
			statement.close();
						
			//遍历设备，挂载PID
			Iterator iterator = idedMap.entrySet().iterator();
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				int id = (Integer)entry.getKey();
				DeviceApp app = (DeviceApp)entry.getValue();
				
				if(pidMap.containsKey(id)){
					List<Integer> ids = pidMap.get(id);
					for(int cid:ids){
						app.addChild(idedMap.get(cid));
						idedMap.get(cid).setParent(app);
					}
				}
			}
			
        	return true;
		} catch (Exception e) {
			return false;
		} 
    }
    
    public boolean loadModelDevice(Connection conn){
    	if(deviceTree == null){
    		deviceTree = new CommonTree<Device>();
    	}
    	CommonTreeNode dvroot = deviceTree.getRoot();
    	Device rootDevice = new Device(0,"设备树","ROOT");
    	dvroot.setData(rootDevice);
    	
    	loadChildDevice(dvroot,conn);
    	return true;
    }	
    
    void loadChildDevice(CommonTreeNode node,Connection conn){
    	try {
    		Statement statement = conn.createStatement();
    		int pid = ((Device)node.getData()).id;
			ResultSet rs = statement.executeQuery("SELECT ID,name,PID,template FROM device where PID="+pid);
			if (rs != null) {
				int id;
				String dev_name,dev_temp;
				while (rs.next()) {
					id = rs.getInt(1);
					dev_name = rs.getString(2);
					pid = rs.getInt(3);
					dev_temp = rs.getString(4);
					
	        		DeviceTemplate template = getTemplate(dev_temp);
	        		
	        		if(template == null){
	        			continue;
	        		}
	        		
	        		Device device = new Device(id,dev_name,dev_temp);
	        		
	    			for(DeviceProperty prop:template.propertyList){
	    				device.addPropertyEdit(prop.name,prop.desc, prop, 0);
	    			}

	        		//load propertys from device tables.
	        		Map<String,Integer> infos = loadProperties(conn,id,template.hasRefProperty,
	        				template.hasIntProperty,template.hasFloatProperty,template.hasStringProperty);
	        		if(infos != null){
		    			Iterator iterator = infos.entrySet().iterator();
		    			while(iterator.hasNext()){
		    				Entry entry = (Entry)iterator.next();
		    				String name = (String)entry.getKey();
		    				Integer key = (Integer)entry.getValue();

		    				device.setKey(name,key);
		    			}
	        		}
	        		
	    			CommonTreeNode n = node.addChild(device);
	    			loadChildDevice(n,conn);
				}
				rs.close();
				rs = null;
			}
			statement.close();
		} catch (Exception e) {
		} 
    }
    
    public CommonTreeNode findModel(CommonTreeNode node,String parent_name,String tname){
        DeviceTemplate template = (DeviceTemplate)node.getData();
        DeviceTemplate pTemplate = null;
        
        CommonTreeNode ttt = node.getParent();
        if(ttt != null){
        	pTemplate = (DeviceTemplate)ttt.getData();
        }        
        
        if(parent_name == null || parent_name.isEmpty()){
	        if(template != null){
		        String name = template.name;
		        if(name.equals(tname)){
		        	return node;
		        }
	        }
        }else{
	        if(template != null){
		        String name = template.name;
		        if(pTemplate == null){
			        if(name.equals(tname)){
			        	return node;
			        }
		        }else{
			        if(name.equals(tname) && pTemplate.name.equals(parent_name)){
			        	return node;
			        }	
		        }
	        }
        }
        
    	List<CommonTreeNode> childs = node.getChildren();  
        if (childs != null) {  
            for (CommonTreeNode n : childs) {  
            	CommonTreeNode t = findModel(n,parent_name,tname);  
    	        if(t != null){
    	        	return t;
    	        }
            }  
        }
        return null;
    }
    
    public CommonTreeNode findDevice(CommonTreeNode node,Device device){
        Device dev = (Device)node.getData();
        
        if(dev == device){
        	return node;
        }
        
    	List<CommonTreeNode> childs = node.getChildren();  
        if (childs != null) {  
            for (CommonTreeNode n : childs) {  
            	CommonTreeNode t = findDevice(n,device);  
    	        if(t != null){
    	        	return t;
    	        }
            }  
        }
        return null;
    }
    
    public void print(CommonTreeNode node){
        List<CommonTreeNode> childs = node.getChildren();  
        DeviceTemplate template = (DeviceTemplate)node.getData();
        String name = "";
        if(template != null){
        	name = template.description;
        }
        
        int level = node.getLevel();
        
        String msg = "";
        for(int i=0;i<level;i++){
        	msg += "\t";  
        }
        msg += String.format("level:%d node:%s childs:%d", node.getLevel(), name, childs == null ? 0 : childs.size());
        System.out.println(msg);  
  
        if (childs != null) {  
            for (CommonTreeNode n : childs) {  
            	print(n);  
            }  
        }  
    }

    public static void main(String[] args){
    	DeviceModels dm = new DeviceModels();
    	dm.loadModel("D:/DCS2000-NCC/HMI/project/曲阳光伏/device");
    	dm.print(dm.models.getRoot());
    }
}
