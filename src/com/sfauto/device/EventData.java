package com.sfauto.device;

import java.util.Date;

import com.sfauto.db.DBField;
import com.sfauto.db.TableDefine;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EventData {
    public static final String TABLE_NAME = "DEV_ALARM";

    public int dev_id,status,type,level;
    public long ltime;
    public String prop_name,content;
    
    public EventData() {
        dev_id = status= type = level = 0;
        ltime = 0;
        
        prop_name = "";
        content = "";
    }
    
    public static String getTableName(long time){
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMM");
        return TABLE_NAME + format.format(new Date(time));
    }
    
    public static String[] getSQL(long time){
    	String tableName = getTableName(time);
        
    	String sql = "create table " + tableName + "(ID INTEGER PRIMARY KEY AUTO_INCREMENT,dev_id INT not null,name VARCHAR(128),"; 
    	sql += "status INT not null,type INT not null,level INT not null,time bigint not null,content VARCHAR(255))";

		String id_index = "create index " + tableName + "_id_idx on " + tableName + "(dev_id)";
		String status_index = "create index " + tableName + "_status_idx on " + tableName + "(status)";
		String type_index = "create index " + tableName + "_type_idx on " + tableName + "(type)";
		String level_index = "create index " + tableName + "_level_idx on " + tableName + "(level)";
		String time_index = "create index " + tableName + "_time_idx on " + tableName + "(time)";
		
		String[] sqls = new String[6];
		sqls[0] = sql;
		sqls[1] = id_index;
		sqls[2] = status_index;
		sqls[3] = type_index;
		sqls[4] = level_index;
		sqls[5] = time_index;
		
		return sqls;
    }
}
