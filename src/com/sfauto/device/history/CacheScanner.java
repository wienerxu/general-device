package com.sfauto.device.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import sun.nio.ch.FileChannelImpl;

import com.sfauto.realdb.JRDBSet;
import com.sfauto.toolkits.threadpool.PooledThread;

public class CacheScanner extends PooledThread {
    Calendar cal;
    public static String bufferPath = JRDBSet.getInstance().ProjectPath + "/historian/";
    public static int MAX_BUFFER_CAPACITY = 200000;

	Method unmapMethod = null;
	SaveWorker worker = null;
	
    public CacheScanner(SaveWorker worker) { 
    	this.worker = worker;
		try {
			unmapMethod = FileChannelImpl.class.getDeclaredMethod("unmap",MappedByteBuffer.class);
			unmapMethod.setAccessible(true);
		} catch (Exception e) {
			unmapMethod = null;
		} 
    }

	public void unmap(MappedByteBuffer buf){
		if(unmapMethod != null){
			try {
				unmapMethod.invoke(FileChannelImpl.class, buf);
			} catch (Exception e) {
			}
		}
	}
	
    public void run() {
        running = true;
        try {
            snapshot();
        } catch (Exception ex) {
        } finally {
            running = false;
        }
    }

    public void destroy() {
    }

    private void snapshot() {
    	int counter = 0;
        File dir = new File(bufferPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
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
        	return;
        }
        Collections.sort(fileList);
        
        int pos_write = 0, pos_read = 0,position;
        long key;
        long time;
        float value;
        boolean isFull = false;
        
        for (int fIndex=0;fIndex<fileList.size();fIndex++) {
        	String file = fileList.get(fIndex)+".dat";
        	
            boolean isDelete = false;
            RandomAccessFile RAFile = null;
            FileChannel fc = null;
            MappedByteBuffer mapBuf = null;
            try {
                RAFile = new RandomAccessFile(bufferPath + file, "rw");
            } catch (FileNotFoundException ex) {
            	continue;
            }             
            try {
				fc = RAFile.getChannel();
				mapBuf = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size());
			} catch (IOException e1) {
				try {
					RAFile.close();
				} catch (IOException e) {
				}
				continue;
			}
            
            //mapBuf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            mapBuf.position(0);
            pos_write = mapBuf.getInt();
            pos_read = mapBuf.getInt();
        	
            while (pos_write != pos_read) {
                if (counter >= MAX_BUFFER_CAPACITY) {
                    isFull = true;
                    break;
                }
                position = 8 + (20 * pos_read);
                mapBuf.position(position);

                key = mapBuf.getLong();
                time = mapBuf.getLong();
                value = mapBuf.getFloat();
                
                worker.bufferSnap(key,time,value);
                
                pos_read++;
                mapBuf.position(4);
                mapBuf.putInt(pos_read);
                counter++;
            }
            if (pos_read >= Cache.MAX_POINT) {
                isDelete = true;
            }

            try {
                unmap(mapBuf);
                mapBuf = null;

            	fc.close();
                RAFile.close();                
            } catch (IOException ex1) {
            }
            if (isDelete) {
           		new File(bufferPath + file).delete();
            }
            if (isFull) {
                break;
            }
        }
    }    
}
