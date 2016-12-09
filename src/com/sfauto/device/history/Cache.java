package com.sfauto.device.history;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import sun.nio.ch.FileChannelImpl;

import com.sfauto.realdb.JRDBSet;
import com.sfauto.toolkits.utils.CriticalSection;

public class Cache {
	public static Cache cache = null;
	Method unmapMethod = null;
	public static String basePath = null; 
	CriticalSection cs = new CriticalSection();
	int fileCounter = 0;
	int writePosition = 0;
    static int length = 4000008;  
    static int MAX_POINT = 200000;
    FileChannel fileChannel;  
	MappedByteBuffer buffer;
	
    public static synchronized Cache getInstance() {
        if (cache == null) {
        	cache = new Cache();
        }
        return cache;
    }
    
    public void close(){
    	closeBuffer();    	
    }
    
    private Cache(){
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
	}
    
	public void unmap(MappedByteBuffer buf){
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
	
    public void cache(long key,long time,float value){
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
    
    void loadBuffer(){
        try {
			fileChannel = new RandomAccessFile(basePath + "/" + fileCounter +".dat", "rw").getChannel();  
			buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, length);
            //buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
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
            //buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
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
