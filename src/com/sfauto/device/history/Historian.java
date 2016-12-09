package com.sfauto.device.history;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sun.nio.ch.FileChannelImpl;

import com.sfauto.device.EventData;
import com.sfauto.device.HisData;
import com.sfauto.realdb.JRDBSet;
import com.sfauto.toolkits.doubledb.ConnectedDB;
import com.sfauto.toolkits.doubledb.DBFactory;
import com.sfauto.toolkits.utils.CriticalSection;

public class Historian {
    public static long MIS_IN_DAY = 86400 * 1000; 	
	public static Historian saver = null;
	SaveWorker worker = null;
	
	public static Historian getInstance(){
		if(saver == null){
			saver = new Historian();
		}
		return saver;
	}
	
	Method unmapMethod = null;
	public static String basePath = null; 
	CriticalSection cs = new CriticalSection();
	int fileCounter = 0;
	int writePosition = 0;
    static int length = 4000008;  
    static int MAX_POINT = 200000;
    FileChannel fileChannel;  
	MappedByteBuffer buffer;
	
    public void close(){
    	worker.destroy();
    	closeBuffer();    
    }
    
    private Historian(){
		try {
			unmapMethod = FileChannelImpl.class.getDeclaredMethod("unmap",MappedByteBuffer.class);
			unmapMethod.setAccessible(true);
		} catch (Exception e) {
			unmapMethod = null;
		}
		basePath = JRDBSet.getInstance().ProjectPath + "/historian";
		if(!new File(basePath).exists()){
			new File(basePath).mkdirs();
		}
		loadFiles();
        loadBuffer();
        
        worker = new SaveWorker();
	}
    
	void unmap(MappedByteBuffer buf){
		if(unmapMethod != null){
			try {
				unmapMethod.invoke(FileChannelImpl.class, buf);
			} catch (Exception e) {
			}
		}
	}
	
	void loadFiles(){
        File dir = new File(basePath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
        	fileCounter = 1;
        	return;
        }
        ArrayList<Integer> fileList = new ArrayList<Integer>();
        int file_number;
        for (File f : files) {
            if (!f.exists() || f.isDirectory() || !f.getName().endsWith(".dat")) {
                continue;
            }
            try {
            	String fName = f.getName();
                file_number = Integer.parseInt(fName.substring(0,fName.length()-4));
                fileList.add(file_number);
            } catch (NumberFormatException ex2) {
            }
        }
        if (fileList.size() == 0) {
        	fileCounter = 1;
        	return;
        }
        Collections.sort(fileList);
        fileCounter = fileList.get(fileList.size()-1);
	}
	
    public void save_trend(long key,long time,float value){
    	cs.enter();
    	
    	buffer.position(8+writePosition*20);
    	buffer.putLong(key);
    	buffer.putLong(time);
    	buffer.putFloat(value);
    	writePosition++;
    	buffer.position(0);
    	buffer.putInt(writePosition);
    	
    	if(writePosition >= MAX_POINT){
    		closeBuffer();
    		createCacheFile();
    	}    	
    	cs.leave();
    }
    
    public void save_alarm(EventData ed){
    	worker.bufferEvent(ed);
    }
    
    public HisData[] query(long key, long start, long end) {
        Connection conn = DBFactory.getInstance().pdb.getFreeConnection("queryData");
        ConnectedDB cdb = null;
        List<HisData> cds = null;
        
        long now = System.currentTimeMillis();
        if(end > now){
        	end = now;
        }
        if (conn != null) {
            cdb = new ConnectedDB(conn);
            try {
				cds = getHisData(key, start, end, cdb);
			} catch (Exception e) {
			} finally{
				cdb.close();
			}
        }
        if(cds != null){
	        HisData[] datas = new HisData[cds.size()];
	        cds.toArray(datas);
	        return datas;
        }else{
        	return null;
        }
    }
    
    List<HisData> getHisData(long key, long start, long end, ConnectedDB cdb) {
        long s = start;
        long e = end;
        long start_of_day = 0;
        long end_of_day = 0;
        long now = System.currentTimeMillis();

        List<HisData> snaps = new ArrayList<HisData>();
        while (true) {
            end_of_day = TimeUtils.endOfDay(s);
            if (end_of_day >= end) {
                start_of_day = TimeUtils.startOfDay(s);
                e = end;
                getSnapData(key, s, e, cdb, snaps);                    
                break;
            } else {
                e = end_of_day;
                start_of_day = TimeUtils.startOfDay(s);
                if (!getSnapData(key, s, e, cdb, snaps)){
                    break;
                }
                s = TimeUtils.startOfDay(s + MIS_IN_DAY);
            }
        }
        return snaps;
    }
    
    boolean getSnapData(long key, long start, long end,ConnectedDB cdb,List<HisData> datas) {
        boolean isOK = true;
        long now = System.currentTimeMillis();
        if (start > now) {
            return false;
        }
        if (end > now) {
            end = now;
            isOK = false;
        }
    	
    	SimpleDateFormat snapDate = new SimpleDateFormat("yyyyMMdd");
    	String sdate = snapDate.format(new Date(start));
		String tableName = "historian" + sdate;
		long day_start = TimeUtils.startOfDay(start);
		
		List<HisData> result = new ArrayList<HisData>();
        String sql = "SELECT DATA FROM " + tableName + " WHERE ID = ?";
        try {
            cdb.prepareStatement(sql);
            cdb.setLong(1, key);
            ResultSet rs = cdb.executeQuery();
            if (rs != null){
            	ByteBuffer buffer = null;
                int length = 0;
                byte[] rb = new byte[4096];
                Blob blob = null;
                InputStream input = null;
                int bs = 0;              	
            	while(rs.next()) {          		
	                blob = rs.getBlob(1);
	                input = blob.getBinaryStream();
	                length = (int) blob.length();
	                buffer = ByteBuffer.allocate(length);
	                buffer.position(0);
	                while ((bs = input.read(rb)) != -1) {
	                    buffer.put(rb, 0, bs);
	                }
	                if (buffer != null) {
	                    buffer = ByteBuffer.wrap(buffer.array());
	                    buffer.order(ByteOrder.LITTLE_ENDIAN);
	                    buffer.position(0);
	                    while (buffer.hasRemaining()) {
	                    	try{
	                    		int time = buffer.getInt();
		                        float value = buffer.getFloat();
		                        result.add(new HisData(time+day_start, value));
	                    	}catch(java.nio.BufferUnderflowException ex){
	                    		break;
	                    	}
	                        
	                    }
	                }
            	}
                rs.close();
                rs = null;
            }
        } catch (IOException ex) {
        } catch (SQLException ex) {
        }
        Collections.sort(result);
        if (result.size() == 0) {
            return isOK;
        }
        HisData ssiStart, ssiEnd,ssi;
        //add start or end points for drawing.
        ssiStart = result.get(0);
        ssiEnd = result.get(result.size() - 1);

        long t1 = 0, t2 = 0;
        float f1 = 0, f2 = 0;
        //remove extra points go beyond from start range and add start point if necessary.
        if (ssiStart.time < start) {
            int pos=0;
            for (pos = 0; pos < result.size(); pos++) {
                ssi = result.get(pos);
                if (ssi.time >= start) {
                    t2 = ssi.time;
                    f2 = ssi.value;
                    break;
                } else {
                    t1 = ssi.time;
                    f1 = ssi.value;
                }
            }
            int size = result.size()-pos;
            Object[] a = result.toArray();
            HisData[] temp = new HisData[size];
            System.arraycopy(a, pos, temp, 0, size);
            result.clear();
            for(int i=0;i<size;i++){
                result.add(temp[i]);
            }
        }
        ssiEnd = result.get(result.size() - 1);
        //remove extra points go beyond from end range and add end point if necessary.
        if (ssiEnd.time > end) {
            int pos=0;
            for (pos = result.size()-1; pos >=0; pos--) {
                ssi = result.get(pos);
                if (ssi.time <= end) {
                    t2 = ssi.time;
                    f2 = ssi.value;
                    break;
                } else {
                    t1 = ssi.time;
                    f1 = ssi.value;
                }
            }
            int size = pos+1;
            Object[] a = result.toArray();
            HisData[] temp = new HisData[size];
            System.arraycopy(a, 0, temp, 0, size);
            result.clear();
            for(int i=0;i<size;i++){
                result.add(temp[i]);
            }
        }
        datas.addAll(result);
        return isOK;
    }
    
    void loadBuffer(){
        try {
			fileChannel = new RandomAccessFile(basePath + "/" + fileCounter +".dat", "rw").getChannel();  
			buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, length);
			buffer.position(0);
			writePosition = buffer.getInt();  //next write position
		} catch (Exception e) {
		} 
    }
    
    boolean createCacheFile(){
        try {
        	fileCounter++;
			fileChannel = new RandomAccessFile(basePath + "/" + fileCounter +".dat", "rw").getChannel();  
			buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, length);
			writePosition = 0;
			return true;
		} catch (Exception e) {
			return false;
		} 
    }
    
    void closeBuffer(){
    	if(buffer != null){
    		unmap(buffer);
    		buffer = null;
    	}
    	if(fileChannel != null){
    		try {
				fileChannel.close();
				fileChannel = null;
			} catch (IOException e) {
			}
    	}
    } 
}
