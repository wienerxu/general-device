package com.sfauto.device;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.sfauto.db.FactoryParam;
import com.sfauto.realdb.JRDBSet;
import com.sfauto.realdb.RDBTools;
import com.sfauto.toolkits.doubledb.DBFactory;
import com.sfauto.toolkits.utils.CriticalSection;

public class DeviceFactory {
	public static DeviceFactory dm = null;
	CriticalSection cs = new CriticalSection();
	DeviceModels models = new DeviceModels();
	static boolean isInitialize = false;
	
	ModelDB modelDB = null;
	String baseDir = null;	
	boolean isRunMode;
	
	Scheduler scheduler = null;
	
    public static synchronized DeviceFactory getInstance() {
        if (dm == null) {
            dm = new DeviceFactory();
        }
        return dm;
    }
    
    private DeviceFactory(){
    }
    
    public DeviceModels getModels(){
    	return models;
    }
    /**
     * 初始化
     * @param path:起始目录，为工程目录的/device目录
     * @param isRunMode:是否为运行态
     */
    public boolean initialize(boolean isRunMode){
    	cs.enter();

    	if(isInitialize){
    		cs.leave();
    		return true;
    	}
    	    	
    	this.isRunMode = isRunMode;
    	
    	initRealDB(); 
    	
    	baseDir = JRDBSet.ProjectPath + "/device";
    	
    	if(!new File(baseDir).exists()){
    		return false;
    	}
    	
    	models.loadModel(baseDir);    	
    	loadPlugins(baseDir);
    	
    	initModelDB();
    	
    	if(isRunMode){
    		initHisDB();
    	}
    	models.loadDevices(modelDB.getConnection(),isRunMode);
    	
    	if(isRunMode){
    		modelDB.closeDB();
    		//启动定时任务
    		startTask();
    	}
    	
    	isInitialize = true;
    	
    	cs.leave();
    	return true;
    }
    
    void startTask(){
		Scheduler scheduler = new Scheduler();
		
    	Iterator iter = models.templateMap.entrySet().iterator();
        while(iter.hasNext()){
        	Entry entry = (Entry)iter.next();
        	String task_name = (String)entry.getKey();
        	DeviceTemplate template = (DeviceTemplate)entry.getValue();

        	if(template.scheduleCycle != null && template.scheduleCommand != null){
	            DeviceTask task = new DeviceTask(task_name,template.scheduleCommand,template.scheduleParameter);
	            scheduler.schedule(template.scheduleCycle, task);
        	}
        }
		scheduler.start();
    }
    
    void stopTask(){
    	if(scheduler != null){
    		scheduler.stop();
    	}
    }
    
    private void initRealDB(){
		RDBTools.getInstance();
    	JRDBSet.getInstance().attachAllDB();
    }
    
    private boolean initHisDB() {
        DBFactory factory = DBFactory.getInstance();
        FactoryParam fp = new FactoryParam(5, 300000);            	
        return factory.init(JRDBSet.getInstance().ProjectPath + "/config/SysConfig.ini", "历史库参数",
                         JRDBSet.getInstance().ProjectPath,fp);
    }
    
    private boolean initModelDB(){
    	modelDB = new ModelDB();
    	boolean isOK = modelDB.openDB(baseDir+"/device.db");
    	if(isOK){
    		modelDB.createModelTables();
    	}
    	return isOK;
    }
    
    void loadPlugins(String path){
        //load module define.
		File config = new File(path,"device.xml");
		if (config.exists() && config.isFile()) {
			SAXBuilder builder = new SAXBuilder();
			Document doc = null;
			try {
				doc = builder.build(config);
			} catch (Exception e1) {
			} 
			if(doc == null){
				return ;
			}
			Element root = doc.getRootElement();
			List apps = root.getChildren();

			if (apps != null) {
				Iterator i = apps.iterator();
				while (i.hasNext()) {
					Element engine = (Element) i.next();
					String deviceName = engine.getAttributeValue("type");
					String class_name = engine.getAttributeValue("class");
					//String parameter = engine.getAttributeValue("parameter");

					if (deviceName != null && class_name != null) {						
						//deviceName = deviceName.toLowerCase();
						DeviceTemplate template = models.getTemplate(deviceName);
						if(template != null){
							//test if class is ready.
		    				boolean isOk = false;
		    				try {
								Class cls_name = Class.forName(class_name);
								DeviceApp app = (DeviceApp) cls_name.newInstance();
								isOk = true;
								app = null;
		    				}catch(Exception e){
		    					e.printStackTrace();
		    				}
		    				if(isOk){
		    					template.hasPlugin = true;		
		    					template.class_name = class_name;
		    				}
						}
					}
				}
			}
		}    	
    }
    
    // 返回指定类型的设备
    public DeviceApp[] getDevices(String templateName){
    	List<DeviceApp> apps = models.templatedMap.get(templateName);
    	if(apps != null){
    		DeviceApp[] devices = new DeviceApp[apps.size()];
    		apps.toArray(devices);
    		return devices;
    	}
    	return null;
    }
    
    //根据id获取设备
    public DeviceApp getDeviceByID(int id){
    	return models.idedMap.get(id);
    }        
    
    public void destroy(){
    	modelDB.closeDB();
    	stopTask();
    }
    
    //下列为编辑态下使用的函数
    public boolean saveModel(Device device,int pid){
    	return modelDB.saveModel(device, pid);
    }
    
    public boolean saveModel(Device device){
    	return modelDB.saveOnModify(device);
    }
    
    public boolean removeDevice(Device device){
    	if(device.id == 0){
    		return false;
    	}
    	models.deviceTree.remove(device);
    	return true;
    }
    
    public Device copyDevice(Device sourceDevice, int pid){
    	return modelDB.copyDevice(sourceDevice, pid);
    }
    
    public boolean renameDevice(Device device,String name){
    	if(device.id == 0){
    		return false;
    	}
    	return modelDB.saveDeviceName(device, name);
    }
    
    public static void main(String[] args){
    	DeviceFactory.getInstance().initialize(true);    	
    	    	
    	while(true){
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
    	}
    }
}
