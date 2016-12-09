package com.sfauto.device.history;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sfauto.db.ConnectionParam;
import com.sfauto.db.DBField;
import com.sfauto.db.TableDefine;
import com.sfauto.device.EventData;
import com.sfauto.toolkits.doubledb.ConnectedDB;
import com.sfauto.toolkits.doubledb.DBFactory;
import com.sfauto.toolkits.threadpool.PooledThread;
import com.sfauto.toolkits.utils.CriticalSection;
import com.sfauto.toolkits.utils.TimeUtils;

public class SaveWorker {
    ExecutorService threadPool = null;
    CacheScanner scanner = null;
    
    CriticalSection csSnap = null;
    CriticalSection csEvent = null;
    
    int snap_counter=0,event_counter=0;

    //define hisdb sequence according to their order.
    public final static int MAIN_DB = 0;
    public final static int BACK_DB = 1;
    
    //按时间分割的数据集，数据集再按照ID分子数据集，实时队列和保存队列。
    HashMap<Long, TimeCatalogedSnapData> snapBuffer = new HashMap<Long, TimeCatalogedSnapData>();
    
    //事项保存需要高速处理，用一个独立线程，不用线程池调度，避免数据库连接过多的情况
    HashMap<Long, TimeCatalogedEventData> EventBuffer = new HashMap<Long, TimeCatalogedEventData>();
    
    ManagerThread manager = null;
    SaveSnapThread snapThread = null;  //由于存储结构的限制，线程不可重入，用独立线程处理 电度量
    SaveEventThread eventThread = null;
    
    long snap_time = System.currentTimeMillis();
    long event_time = System.currentTimeMillis();
    
    public static int MAX_BUFFERED_SNAP = 10000;
    public static int MAX_BUFFERED_EVENT = 200;
    
    //max buffered time,in miseconds
    public static int MAX_BUFFERED_SNAP_TIME = 300000;
    public static int MAX_BUFFERED_EVENT_TIME = 60000;
      
    public SaveWorker() {
    	threadPool = Executors.newCachedThreadPool();
    	
        csSnap = new CriticalSection();
        csEvent = new CriticalSection();
        
        snapThread = new SaveSnapThread();
        eventThread = new SaveEventThread();
        
        scanner = new CacheScanner(this);
        
        manager = new ManagerThread();
        manager.start();
    }

    public void destroy(){
    	manager.running = false;
    	while(manager.isAlive()){
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
    	}  	
    }
    
    class ManagerThread extends Thread {
        boolean running; //退出条件
        public ManagerThread(){
        	running = true;
        }
        public void run() {
        	boolean isFull = false;
        	boolean isTimeout = false;    
        	long now;
            while (running) {
            	now = System.currentTimeMillis();                	
            	try {
            		if(!snapThread.isRunning()){
						isFull = false;
						isTimeout = false;
						if(snap_counter > MAX_BUFFERED_SNAP){
							isFull = true;
						}else if((now - snap_time) > MAX_BUFFERED_SNAP_TIME && snap_counter > 0) {
							isTimeout = true;
						}
						if(isFull || isTimeout){
							threadPool.execute(snapThread);
						}
            		}
				} catch (Exception e) {
					System.err.println(e);
				}                	
				try {
					if(!eventThread.isRunning()){
						isFull = false;
						isTimeout = false;					
						if(event_counter > MAX_BUFFERED_EVENT){  //缓冲满
							isFull = true;
						}else if( event_counter > 0 && (Math.abs((now - event_time)) > MAX_BUFFERED_EVENT_TIME)  ) {//当前时间超出上次存盘时间，且个数大于0
							isTimeout = true;
						}
						if(isFull || isTimeout){
							threadPool.execute(eventThread);
						}
					}
				} catch (Exception e) {
					System.err.println(e);
				}
				
				if(!scanner.isRunning()){
					threadPool.execute(scanner);
				}
				
                try {
					sleep(1000);
				} catch (Exception e) {
				}
                if (!running) {
                    return;
                }
            }
        }
    }
    
	class SaveSnapThread extends PooledThread{
		public SaveSnapThread(){
		}
		
	    public void run() {
	    	if(running){
	    		return;
	    	}
	        running = true;
	        try {
	        	serializeSnap(dumpSnap());
	        } catch (Exception ex) {
	        } finally {
	            running = false;
	        }
	    }
	}
	
	class SaveEventThread extends PooledThread{
		public SaveEventThread(){
		}
		
	    public void run() {
	    	if(running){
	    		return;
	    	}
	    	running = true;
	        try {
	        	serializeEvent(dumpEvent());
	        } catch (Exception ex) {
	        } finally {
	            running = false;
	        }
	    }
	}
	
    public void bufferSnap(long key,long time,float value) {
        csSnap.enter();
        long t = TimeUtils.startOfDay(time);
        
        TimeCatalogedSnapData catalog = snapBuffer.get(t);
        if (catalog == null) {
            catalog = new TimeCatalogedSnapData();
            snapBuffer.put(t, catalog);
        }
        
        catalog.put(key,time,value);
        snap_counter++;
        csSnap.leave();
    }
    
    public void bufferEvent(EventData event) {
        csEvent.enter();
        long time = TimeUtils.startOfDay(event.ltime);
        TimeCatalogedEventData catalog = EventBuffer.get(time);
        if (catalog == null) {
            catalog = new TimeCatalogedEventData();
            EventBuffer.put(time, catalog);
        }
        catalog.put(event);
        event_counter++;
        csEvent.leave();
    }
    
    public HashMap<Long, TimeCatalogedSnapData> dumpSnap(){
    	HashMap<Long, TimeCatalogedSnapData> temp = new HashMap<Long, TimeCatalogedSnapData>();
    	csSnap.enter();
        Iterator iter = snapBuffer.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            TimeCatalogedSnapData catalog = (TimeCatalogedSnapData) entry.getValue();
            temp.put((Long)entry.getKey(),catalog.dumpout());
            iter.remove();
        }
        snap_counter = 0;
        csSnap.leave();
    	return temp;
    }
    
    public HashMap<Long, TimeCatalogedEventData> dumpEvent(){
    	HashMap<Long, TimeCatalogedEventData> temp = new HashMap<Long, TimeCatalogedEventData>();
    	csEvent.enter();
        Iterator iter = EventBuffer.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            TimeCatalogedEventData catalog = (TimeCatalogedEventData) entry.getValue();
            temp.put((Long)entry.getKey(),catalog.dumpout());
            iter.remove();
        }
        event_counter = 0;
        csEvent.leave();
    	return temp;
    }    
    
    //保存量测
    public void serializeSnap(HashMap<Long, TimeCatalogedSnapData> snaps) {
        boolean isMain = false, isBack = false;
        if ((DBFactory.getInstance().configedDB & (1 << 0)) != 0) {
            isMain = true;
        }
        if ((DBFactory.getInstance().configedDB & (1 << 1)) != 0) {
            isBack = true;
        }
        if (isMain || isBack) {
            Iterator iter = snaps.entrySet().iterator();
            long time;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                time = (Long) entry.getKey();
                TimeCatalogedSnapData catalog = (TimeCatalogedSnapData) entry.getValue();
                
                if (isMain) {
               		saveSnap(time,catalog,MAIN_DB);
                }
                if (isBack) {
               		saveSnap(time,catalog,BACK_DB);
                }   
                
                catalog.destroy();
                iter.remove();
            }
        }
        snap_time = System.currentTimeMillis();
    }
        
    //保存报警
    public void serializeEvent(HashMap<Long, TimeCatalogedEventData> snaps) {
        boolean isMain = false, isBack = false;
        if ((DBFactory.getInstance().configedDB & (1 << 0)) != 0) {
            isMain = true;
        }
        if ((DBFactory.getInstance().configedDB & (1 << 1)) != 0) {
            isBack = true;
        }
        if (isMain || isBack) {
            Iterator iter = snaps.entrySet().iterator();
            long time;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                time = (Long) entry.getKey();
                TimeCatalogedEventData catalog = (TimeCatalogedEventData) entry.getValue();
                if (isMain) {
               		saveEvent(time,catalog,MAIN_DB);
                }
                if (isBack) {
               		saveEvent(time,catalog,BACK_DB);
                }            
                catalog.destroy();
                iter.remove();
            }
        }
    }
    
    public void saveSnap(long time, TimeCatalogedSnapData buffer, int dbNo) {   	
    	if(buffer.buffer.entrySet().size()==0){
    		return;
    	}    	
    	SimpleDateFormat snapDate = new SimpleDateFormat("yyyyMMdd");
    	String date = snapDate.format(new Date(time));
		Connection conn = null;
		ConnectedDB cdb = null;
		int db_type = 0;
		// 连接数据库，如果未连接则退出
		long start = System.currentTimeMillis();
		
		conn = DBFactory.getInstance().pdb.getFreeConnection(dbNo);
		if (conn != null) {
			cdb = new ConnectedDB(conn);
			db_type = DBFactory.getInstance().pdb.getConnectionParam(dbNo).getDbType();
		} else {
			return;
		}
		if (!cdb.isValid(db_type)) {
			cdb.close();	
			return;
		} 
		
		String SQL;
		String tableName = "historian" + date;
		boolean isTableExist = cdb.isTableExist(tableName);
		
		if (!isTableExist) {
            TableDefine td = new TableDefine(tableName, null);
            td.addField(new DBField("ID", DBField.BIGINT, 40, false));
            td.addField(new DBField("DATA", DBField.BLOB, 24, true));
            td.key = "ID";
            SQL = td.getCreateSQL(db_type, tableName);
			try {
				cdb.executeCommand(SQL);
			} catch (SQLException ex1) {
				cdb.close();
				return;
			}
		}
		
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psUpdate = null;
		if (isTableExist) {
			SQL = "select ID from " + tableName + " where ID = ?";
			try {
				psSelect = conn.prepareStatement(SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			} catch (SQLException ex3) {
				cdb.close();
				return;
			}
			if (db_type == ConnectionParam.DB_TYPE_ORACLE) {
				try {
					SQL = "SELECT DATA FROM " + tableName + " where ID=? FOR UPDATE";
					psUpdate = conn.prepareStatement(SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				} catch (SQLException ex2) {
					cdb.close();
					return;
				}
			} else {
				try {
					SQL = "update " + tableName + " set DATA=concat(DATA,?) where ID=?";
					psUpdate = conn.prepareStatement(SQL);
				} catch (SQLException ex2) {
					cdb.close();	
					return;
				}
			}
		}
		try {
			SQL = "insert into " + tableName + " values(?,?)";
			psInsert = conn.prepareStatement(SQL);
		} catch (SQLException ex2) {
			cdb.close();	
			return;
		}
		
        Iterator iter = buffer.buffer.entrySet().iterator();
        long key;
        ArrayList<SnapData> ssiList;
        ByteBuffer buf = null;
        ResultSet resultSet = null;
        byte[] data_buffer = null;
        
        int totalSize = buffer.buffer.size();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            key = (Long) entry.getKey();
            CatalogedSnapData csd = (CatalogedSnapData) entry.getValue();

            ssiList = csd.ssiList;
            buf = java.nio.ByteBuffer.allocate((ssiList.size()) * 8);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.position(0);
            for (SnapData sd : ssiList) {
                buf.putInt((int) (sd.time - time));
                buf.putFloat(sd.value);
            }
			data_buffer = buf.array();
			
			if (!isTableExist) {
				try {
					psInsert.setLong(1, key);
					ByteArrayInputStream bis = new ByteArrayInputStream(data_buffer);
					psInsert.setBinaryStream(2, bis, data_buffer.length);
					psInsert.executeUpdate();
					cdb.commit();
				} catch (SQLException ex4) {
					continue;
				}
			} else {
				try {
					psSelect.setLong(1, key);
					resultSet = psSelect.executeQuery();
				} catch (SQLException ex5) {
					continue;
				}
				boolean isUpdate = false;
				try {
					if (resultSet != null && resultSet.next()) {
						isUpdate = true;
						resultSet.close();
					}					
				} catch (SQLException ex7) {
					continue;
				}
				if (isUpdate) {
					if (db_type == ConnectionParam.DB_TYPE_ORACLE) {
						try {
							conn.setAutoCommit(false);
							long pos = 0;
							psUpdate.setLong(1, key);
							ResultSet rset = psUpdate.executeQuery();
							if (rset != null && rset.next()) {
								//rset.updateRow();
								Blob dest_loc = rset.getBlob(1);
								pos = dest_loc.length();
								dest_loc.setBytes(pos + 1, data_buffer);
								conn.commit();
							}
							conn.setAutoCommit(true);
						} catch (SQLException ex4) {
							continue;
						}
					} else {
						try {
							psUpdate.setLong(2, key);
							ByteArrayInputStream bis = new ByteArrayInputStream(data_buffer);
							psUpdate.setBinaryStream(1, bis, data_buffer.length);
							psUpdate.executeUpdate();
							cdb.commit();
						} catch (SQLException ex4) {
							continue;
						}
					}
				} else { // need for insert
					try {
						psInsert.setLong(1, key);
						ByteArrayInputStream bis = new ByteArrayInputStream(data_buffer);
						psInsert.setBinaryStream(2, bis, data_buffer.length);
						psInsert.executeUpdate();
						cdb.commit();
					} catch (SQLException ex4) {
						continue;
					}
				}
			}
        }
		try {
			if (psSelect != null) {
				psSelect.close();
				psSelect = null;
			}
			if (psInsert != null) {
				psInsert.close();
				psInsert = null;
			}
			if (psUpdate != null) {
				psUpdate.close();
				psUpdate = null;
			}
		} catch (SQLException ex) {
		}
		try {
			cdb.close();
		} catch (Exception e1) {
		}
	}  
    
    public void saveEvent(long time, TimeCatalogedEventData buffer, int dbNo) {
    	if(buffer.buffer.size()==0){
    		return;
    	}    	
		Connection conn = null;
		ConnectedDB cdb = null;
		int db_type = 0;
		// 连接数据库，如果未连接则退出
		conn = DBFactory.getInstance().pdb.getFreeConnection(dbNo);
		if (conn != null) {
			cdb = new ConnectedDB(conn);
			db_type = DBFactory.getInstance().pdb.getConnectionParam(dbNo).getDbType();
		} else {
			return;
		}
		if (!cdb.isValid(db_type)) {
			cdb.close();	
			return;
		}        
		
		String table_name = EventData.getTableName(time);
		if (!cdb.isTableExist(table_name)) {
		    try {
			    String [] sqls = EventData.getSQL(time);
		        if (sqls != null) {
		            for (String s : sqls) {
		                cdb.executeCommand(s);
		            }
		        }
		    } catch (Exception ex) {
		    	cdb.close();
		    	return;
		    }
		} 
		try {
		    cdb.setAutoCommit(false);
		    cdb.prepareStatement("insert into " + table_name + " (dev_id,status,type,level,time,name,content) values (?,?,?,?,?,?,?)");
		
			Iterator iter = buffer.buffer.iterator();
		    while (iter.hasNext()) {
		        EventData so = (EventData)iter.next();
		        
		        cdb.setInt(1, so.dev_id);
		        cdb.setInt(2, so.status);
		        cdb.setInt(3, so.type);
		        cdb.setInt(4, so.level);
		        cdb.setLong(5, so.ltime);
		        cdb.setString(6, so.prop_name);
		        cdb.setString(7, so.content);
		        cdb.addBatch();
		    }
		    cdb.executeBatch();
		    cdb.setAutoCommit(true);
		} catch (Exception ex1) {
		    System.err.println(ex1);		    
		}
		try {
			cdb.close();
		} catch (Exception e1) {
			System.err.println("cdb.close err:"+e1);
		}
	}     
        
    //某一点的队列，用于保存模拟量和电度量
    class CatalogedSnapData {
        ArrayList<SnapData> ssiList = new ArrayList<SnapData>();

        public CatalogedSnapData dumpout(){
        	CatalogedSnapData temp = new CatalogedSnapData();
        	temp.ssiList = (ArrayList<SnapData>)this.ssiList.clone();
        	ssiList.clear();
        	return temp;
        }
    }


    //按时间（日）组织的数据
    class TimeCatalogedSnapData {
        public HashMap<Long, CatalogedSnapData> buffer = new HashMap<Long, CatalogedSnapData>();
        public TimeCatalogedSnapData() {
        }
      
        public void put(long key,long time,float value) { //过滤处理
            CatalogedSnapData catalogedSnapData = buffer.get(key);
            if (catalogedSnapData == null) {
                catalogedSnapData = new CatalogedSnapData();
                buffer.put(key, catalogedSnapData);
            }
            ArrayList<SnapData> ssiList = catalogedSnapData.ssiList;
            ssiList.add(new SnapData(time, value));
        }

        public void destroy() {
            Iterator iter = buffer.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                CatalogedSnapData catalogedSnapData = (CatalogedSnapData) entry.getValue();
                catalogedSnapData.ssiList.clear();
                iter.remove();
            }
            buffer.clear();
        }
        
        public TimeCatalogedSnapData dumpout(){
        	TimeCatalogedSnapData tcsd = new TimeCatalogedSnapData();
            Iterator iter = buffer.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                CatalogedSnapData catalogedSnapData = (CatalogedSnapData) entry.getValue();
                tcsd.buffer.put((Long)entry.getKey(),catalogedSnapData.dumpout());
                iter.remove();
            }      
            buffer.clear();
            return tcsd;
        }
    }

    class TimeCatalogedEventData {
        public ArrayList<EventData> buffer = new ArrayList<EventData>();
        public TimeCatalogedEventData() {
        }

        public void put(EventData event) {
        	buffer.add(event);
        }

        public void destroy() {
            buffer.clear();
        }
        
        public TimeCatalogedEventData dumpout(){
        	TimeCatalogedEventData temp = new TimeCatalogedEventData();
        	temp.buffer = (ArrayList<EventData>) buffer.clone();        	
            buffer.clear();
            return temp;
        }        
    }	   
}
