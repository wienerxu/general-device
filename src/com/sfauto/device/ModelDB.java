package com.sfauto.device;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map.Entry;

public class ModelDB {
	static boolean isDriverOK = false;
	Statement statement = null;
	PreparedStatement prepStatement = null;
	Connection connection = null;
	boolean isConnected = false;

	static {
		try {
			Class.forName("org.sqlite.JDBC");
			isDriverOK = true;
		} catch (ClassNotFoundException e) {
			isDriverOK = false;
		}
	}

	public ModelDB() {

	}

	public boolean openDB(String path) {
		if (isConnected) {
			return true;
		}
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + path);
			connection.setAutoCommit(false);
		} catch (SQLException e) {
		}
		isConnected = connection != null;
		return isConnected;
	}

	public boolean closeDB() {
		if (isConnected) {
			try {
				connection.setAutoCommit(true);
				statement = connection.createStatement();
				statement.executeUpdate("VACUUM");				
				statement.close();	
				
				connection.close();
				isConnected = false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return !isConnected;
	}

	public Connection getConnection(){
		if(isConnected){
			return connection;
		}
		return null;
	}
	
	boolean isTableExist(String tableName) {
		if (!isConnected) {
			return false;
		}
		int count = 0;
		try {
			statement = connection.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT COUNT(*)  as CNT FROM sqlite_master where type='table' and name='" + tableName + "'");
			if (rs != null && rs.next()) {
				count = rs.getInt(1);
			}
			if (rs != null) {
				rs.close();
			}
			statement.close();
		} catch (SQLException e) {
		}
		return count > 0;
	}

	public void createModelTables() {
		createDevice();
		createRef();
		createInt();
		createFloat();
		createString();
	}

	void createDevice(){
		if (isConnected && !isTableExist("device")) {
			try {
				statement = connection.createStatement();
				statement
						.executeUpdate("create table device (ID INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(128) not null, PID INT not null, template VARCHAR(128) not null)");
				connection.commit();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
	}

	void createRef(){
		if (isConnected && !isTableExist("dev_reference")) {
			try {
				statement = connection.createStatement();
				statement
						.executeUpdate("create table dev_reference (name VARCHAR(128) not null, dev_id INT not null, record_id int)");
				statement
						.executeUpdate("create unique index dev_reference_idx on dev_reference (dev_id,name)");
				connection.commit();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
	}

	void createInt(){
		if (isConnected && !isTableExist("dev_int")) {
			try {
				statement = connection.createStatement();
				statement
						.executeUpdate("create table dev_int (ID INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(128) not null, dev_id INT not null, value int default 0)");
				statement
						.executeUpdate("create unique index dev_int_idx on dev_int (dev_id,name)");
				connection.commit();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
	}

	void createFloat(){
		if (isConnected && !isTableExist("dev_float")) {
			try {
				statement = connection.createStatement();
				statement
						.executeUpdate("create table dev_float (ID INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(128) not null, dev_id INT not null, value float default 0.0)");
				statement
						.executeUpdate("create unique index dev_float_idx on dev_float (dev_id,name)");
				connection.commit();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		} 
	}

	void createString(){
		if (isConnected && !isTableExist("dev_string")) {
			try {
				statement = connection.createStatement();
				statement
						.executeUpdate("create table dev_string (ID INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(128) not null, dev_id INT not null, value VARCHAR(255))");
				statement
						.executeUpdate("create unique index dev_string_idx on dev_string (dev_id,name)");
				connection.commit();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
	}
	
    public boolean saveModel(Device device,int pid){
    	if(connection == null){
    		return false;
    	}
    	if(device.id == -1){  //create
    		return saveOnCreate(device,pid)!=0;
    	}else{
    		return saveOnModify(device);
    	}
    }
	
    public Device copyDevice(Device sourceDevice, int pid){
    	Device newDevice = sourceDevice.clone();
    	if(saveOnCreate(newDevice,pid)>0){
    		return newDevice;
    	}else{
    		return null;
    	}
    }
    
    int saveOnCreate(Device device,int pid){
		try {
			String SQL = "insert into device(name,PID,template) values(?,?,?)";
			PreparedStatement psInsert = connection.prepareStatement(SQL);
			
			psInsert.setString(1, device.name);
			psInsert.setInt(2,pid);
			psInsert.setString(3,device.template);
			psInsert.executeUpdate();
			
			int dev_id = 0;
			ResultSet rs = psInsert.getGeneratedKeys();   
			if (rs.next()) {
				dev_id = rs.getInt(1);
				device.id = dev_id;
			}
			rs.close();
			psInsert.close();
			
			PreparedStatement psRefInsert = connection.prepareStatement("insert into dev_reference(name,dev_id,record_id) values(?,?,?)");
			PreparedStatement psIntInsert = connection.prepareStatement("insert into dev_int(name,dev_id,value) values(?,?,?)");
			PreparedStatement psFloatInsert = connection.prepareStatement("insert into dev_float(name,dev_id,value) values(?,?,?)");
			PreparedStatement psStringInsert = connection.prepareStatement("insert into dev_string(name,dev_id,value) values(?,?,?)");

			Iterator iterator = device.fields.entrySet().iterator();
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				String name = (String)entry.getKey();
				Variable var = (Variable)entry.getValue();
				
				if(var.type == DeviceProperty.PROPERTY_TYPE_REF){
					psRefInsert.setString(1, name);
					psRefInsert.setInt(2,dev_id);
					psRefInsert.setInt(3,var.key);
					psRefInsert.executeUpdate();
				}else if(var.type == DeviceProperty.PROPERTY_TYPE_FLOAT){
					psFloatInsert.setString(1, name);
					psFloatInsert.setInt(2,dev_id);
					float value = (float)var.getValue();
					psFloatInsert.setFloat(3, value);
					psFloatInsert.executeUpdate();
					
					rs = psFloatInsert.getGeneratedKeys();   
					if (rs.next()) {
						int nKey = rs.getInt(1);
						var.key = nKey;
					}
					var.setValue(value);
					rs.close();	
				}else if(var.type == DeviceProperty.PROPERTY_TYPE_INT){
					psIntInsert.setString(1, name);
					psIntInsert.setInt(2,dev_id);
					int value = (int)var.getValue();
					psIntInsert.setInt(3, value);
					psIntInsert.executeUpdate();
					
					rs = psIntInsert.getGeneratedKeys();   
					if (rs.next()) {
						int nKey = rs.getInt(1);
						var.key = nKey;
					}
					var.setValue(value);
					rs.close();						
				}else if(var.type == DeviceProperty.PROPERTY_TYPE_STRING){
					psStringInsert.setString(1, name);
					psStringInsert.setInt(2,dev_id);
					String value = (String)var.getValue();
					psStringInsert.setString(3, value);
					psStringInsert.executeUpdate();
					
					rs = psStringInsert.getGeneratedKeys();   
					if (rs.next()) {
						int nKey = rs.getInt(1);
						var.key = nKey;
					}
					var.setValue(value);
					rs.close();	
				}		
			}			
			connection.commit();
			
			psRefInsert.close();
			psIntInsert.close();
			psFloatInsert.close();
			psStringInsert.close();
			
			return dev_id;
		} catch (SQLException ex4) {
			ex4.printStackTrace();
			return 0;
		}    
    }
  
    boolean saveDeviceName(Device device,String name){
		try {
			PreparedStatement psUpdate = connection.prepareStatement("update device set name = ? where id = ?");
			psUpdate.setString(1,name);
			psUpdate.setInt(2,device.id);
			psUpdate.executeUpdate();
			connection.commit();			
			psUpdate.close();

			device.name = name;
			return true;
		} catch (SQLException ex4) {
			ex4.printStackTrace();
			return false;
		}   
    }
    
    boolean saveOnModify(Device device){
		try {
			PreparedStatement psUpdate = connection.prepareStatement("update dev_reference set record_id = ? where dev_id = ? and name = ? ");
			Iterator iterator = device.fields.entrySet().iterator();
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				String name = (String)entry.getKey();
				Variable var = (Variable)entry.getValue();
				
				if(var.type == DeviceProperty.PROPERTY_TYPE_REF){
					psUpdate.setInt(1,var.key);
					psUpdate.setInt(2,device.id);
					psUpdate.setString(3, name);					
					
					psUpdate.executeUpdate();
				}	
			}			
			connection.commit();			
			psUpdate.close();
			
			psUpdate = connection.prepareStatement("update dev_int set value = ? where dev_id = ? and name = ? ");
			iterator = device.fields.entrySet().iterator();
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				String name = (String)entry.getKey();
				Variable var = (Variable)entry.getValue();
				
				if(var.type == DeviceProperty.PROPERTY_TYPE_INT){
					psUpdate.setInt(1,(int)var.getValue());
					psUpdate.setInt(2,device.id);
					psUpdate.setString(3, name);					
					
					psUpdate.executeUpdate();
				}	
			}			
			connection.commit();			
			psUpdate.close();
			
			psUpdate = connection.prepareStatement("update dev_float set value = ? where dev_id = ? and name = ? ");
			iterator = device.fields.entrySet().iterator();
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				String name = (String)entry.getKey();
				Variable var = (Variable)entry.getValue();
				
				if(var.type == DeviceProperty.PROPERTY_TYPE_FLOAT){
					psUpdate.setFloat(1,(float)var.getValue());
					psUpdate.setInt(2,device.id);
					psUpdate.setString(3, name);					
					
					psUpdate.executeUpdate();
				}	
			}			
			connection.commit();			
			psUpdate.close();
			
			psUpdate = connection.prepareStatement("update dev_string set value = ? where dev_id = ? and name = ? ");
			iterator = device.fields.entrySet().iterator();
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				String name = (String)entry.getKey();
				Variable var = (Variable)entry.getValue();
				
				if(var.type == DeviceProperty.PROPERTY_TYPE_STRING){
					psUpdate.setString(1,var.getValue().toString());
					psUpdate.setInt(2,device.id);
					psUpdate.setString(3, name);					
					
					psUpdate.executeUpdate();
				}	
			}			
			connection.commit();			
			psUpdate.close();
			
			return true;
		} catch (SQLException ex4) {
			ex4.printStackTrace();
			return false;
		}    
    }
    
    public boolean removeDevice(Device device){
		try {
			int dev_id = device.id;
			
			Statement sDelete = connection.createStatement();
			
			int c = sDelete.executeUpdate("delete from device where ID = " + dev_id);
			connection.commit();
			
			c = sDelete.executeUpdate("delete from dev_reference where dev_id="+dev_id);
			connection.commit();
			
			c = sDelete.executeUpdate("delete from dev_int where dev_id="+dev_id);
			connection.commit();
			
			c = sDelete.executeUpdate("delete from dev_float where dev_id="+dev_id);
			connection.commit();
			
			c = sDelete.executeUpdate("delete from dev_string where dev_id="+dev_id);
			connection.commit();
			
			sDelete.close();
			return true;
		} catch (SQLException ex4) {
			ex4.printStackTrace();
			return false;
		}        
    }
    
	public static void main(String[] args) {
		ModelDB model = new ModelDB();
		long s = System.currentTimeMillis();
		if (model.openDB("D:/DCS2000-NCC/HMI/project/v6project/device/device.db")) {
			//model.createModelTables();
			model.closeDB();
		}
		long e = System.currentTimeMillis();
		System.out.println(e-s);
		System.exit(0);
	}
}
