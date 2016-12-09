package com.sfauto.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.sfauto.toolkits.utils.CriticalSection;

public class DeviceScript {
	ScriptEngine jsEngine = null;
	Invocable invoker = null;
	
    String lastError = "";
    boolean isOk = false;
    CriticalSection cs = null;
    
    public DeviceScript(File file){
    	isOk = initialize(file);
    	cs = new CriticalSection();
    }
    
    boolean initialize(File file) {
        jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        try {
            if (file.exists()) {
                FileInputStream fis = null;
                Reader reader = null;
                fis = new FileInputStream(file);
                reader = new InputStreamReader(fis);
                
                jsEngine.eval(reader);
                invoker = (Invocable) jsEngine;
                
                reader.close();
                fis.close();
            }
        } catch (Exception ex) {
            lastError = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public Result runScript(DeviceApp device,String command,String parameter) {
    	if(!isOk){
    		return new Result(false,"load script file fails!");
    	}
    	if(command == null || command.length()==0){
    		return new Result(false,"empty command!");
    	}
    	if(device == null){
    		return new Result(false,"device is null!");
    	}
    	cs.enter();
        try {
        	jsEngine.put("me", device);            
            
            if(parameter == null){
            	parameter = "";
            }
            String returnValue = (String)invoker.invokeFunction(command, parameter);
            
            if(returnValue != null){
            	if(returnValue.compareToIgnoreCase("ok") == 0){
            		return new Result(true,"");
            	}else{
            		return new Result(false,returnValue);
            	}
            }
            return new Result(false,"return value is null!");
        } catch (Exception ex) {
            lastError = ex.getMessage();
            return new Result(false,lastError);
        } finally{
        	jsEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        	cs.leave();
        }        
    }
}
