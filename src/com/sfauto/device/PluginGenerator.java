package com.sfauto.device;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.sfauto.realdb.DBTable;
import com.sfauto.realdb.JRDBSet;
import com.sfauto.realdb.record.TableRecord;

public class PluginGenerator {
	DeviceTemplate template = null;
	String error = "";

	public PluginGenerator() {
	}

	public void generate(String model, String filePath, String className) {
		if (!loadModel(model)) {
			System.out.println(error);
			return;
		}
		if (template != null) {
			File path = new File(filePath);
			if (path.exists() && path.isDirectory()) {
				generateClass(filePath, className);
			} else {
				System.out.println("生成目录不存在！");
			}
		} else {
			System.out.println("读模板文件[" + model + "]失败！");
		}
	}

	/**
	 * load device model from files,generate device catalogs.
	 * 
	 * @return
	 */
	boolean loadModel(String path) {
		File file = new File(path);
		if (!file.exists()) {
			error = "模板文件[" + path + "]不存在！";
			return false;
		}
		String file_name = file.getName();
		// file_name = file_name.toLowerCase();
		if (file_name.endsWith(".xml")) {
			try {
				String item = null;

				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(file);
				Element root = doc.getRootElement();

				template = new DeviceTemplate();

				List properties = root.getChildren();

				if (properties != null) {
					Iterator i = properties.iterator();
					while (i.hasNext()) {
						Element e_property = (Element) i.next();
						String type = e_property.getAttributeValue("type");
						String desc = e_property.getAttributeValue("desc");
						String name = e_property.getAttributeValue("name");

						DeviceProperty property = new DeviceProperty();
						property.name = name;
						property.desc = desc;

						if (type.compareToIgnoreCase("point") == 0) {
							property.type = property.PROPERTY_TYPE_REF;
							String db = e_property.getAttributeValue("RDDB");
							String table = e_property
									.getAttributeValue("RDTable");
							String field = e_property
									.getAttributeValue("RDField");
							property.db = db;
							property.table = table;
							property.field = field;
							property.isReference = true;
							template.hasRefProperty = true;
						} else if (type.compareToIgnoreCase("int") == 0) {
							property.type = property.PROPERTY_TYPE_INT;
							property.defaultValue = e_property
									.getAttributeValue("RdefValue");
							template.hasIntProperty = true;
						} else if (type.compareToIgnoreCase("float") == 0) {
							property.type = property.PROPERTY_TYPE_FLOAT;
							property.defaultValue = e_property
									.getAttributeValue("RdefValue");
							template.hasFloatProperty = true;
						} else if (type.compareToIgnoreCase("string") == 0) {
							property.type = property.PROPERTY_TYPE_STRING;
							property.defaultValue = e_property
									.getAttributeValue("RdefValue");
							template.hasStringProperty = true;
						} else {
							continue;
						}
						template.addProperty(property);
					}
				}
				return true;
			} catch (Exception e) {
				error = e.getMessage();
			}
		} else {
			error = "模板文件[" + path + "]不是xml！";
		}
		return false;
	}

	boolean generateClass(String path, String className) {
		File file = new File(path, className + ".java");
		if (file.exists()) {
			file.delete();
		}
		Writer out = new Writer(file.getAbsolutePath());

		out.println("package com.sfauto.device.plugin;");
		out.println("");

		out.println("import com.sfauto.device.CommonDevice;");
		out.println("import com.sfauto.device.Result;");
		out.println("import com.sfauto.device.Device;");

		out.println("");

		out.println("public class " + className + " extends CommonDevice{");

		out.println("");

		out.println("	public " + className + "(){");
		out.println("		super();");
		out.println("	}");
		out.println("");

		Iterator iterator = template.properties.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry) iterator.next();
			String name = (String) entry.getKey();
			DeviceProperty property = (DeviceProperty) entry.getValue();
			String dataType = null;

			if (property.isReference) {
				try {
					DBTable table = JRDBSet.getInstance().getTable(
							property.getDB(), property.getTable());
					if (table != null) {
						if (table.fieldIndexMap.size() == 0) {
							table.mapFieldIndex(TableRecord.name_mode);
						}
						TableRecord record = table.create_record();
						String[] types = record.getFields(record.type_mode);
						int field_index = table.getFieldIndex(property
								.getField());
						String type = types[field_index];
						dataType = getJavaType(type);
					} else {
						System.out.println(property.name + "@"
								+ property.getDB() + "\t" + property.getTable()
								+ " is not found in rtdb!");
					}
				} catch (Exception e) {
					System.out.println("parse property " + property.name
							+ " error");
					e.printStackTrace();
				}
			} else {
				if (property.type == property.PROPERTY_TYPE_INT) {
					dataType = "Integer";
				} else if (property.type == property.PROPERTY_TYPE_FLOAT) {
					dataType = "Float";
				} else if (property.type == property.PROPERTY_TYPE_STRING) {
					dataType = "String";
				}
			}

			if (dataType != null) {
				out.println("	public " + dataType + " get_" + property.name
						+ "(){");
				out.println("		return (" + dataType + ")get(\"" + property.name
						+ "\");");
				out.println("	}");
				out.println("");

				out.println("	public boolean set_" + property.name + "("
						+ dataType + " _" + property.name + "){");
				out.println("		return set(\"" + property.name + "\",_"
						+ property.name + ");");
				out.println("	}");
				out.println("");
			}
		}

		// internal properties
		out.println("	public String get_my_name(){");
		out.println("		return (String)get(\"my_name\");");
		out.println("	}");
		out.println("");

		out.println("	@Override");
		out.println("	public Result operate(String command,String parameter) {");
		out.println("		if(getScript() != null){");
		out.println("			return getScript().runScript(this, command, parameter);");	
		out.println("		}else{");		
		out.println("			Result result = new Result();");
		out.println("			result.isSuccess = true;");
		out.println("			result.info = \"ok\";");
		out.println("			return result;");
		out.println("		}");
		out.println("	}");

		out.println("}");

		out.close();
		return true;
	}

	String getJavaType(String type) {
		if (type.compareToIgnoreCase("int") == 0
				|| type.compareToIgnoreCase("uint") == 0) {
			return "Integer";
		} else if (type.compareToIgnoreCase("float") == 0) {
			return "Float";
		} else if (type.compareToIgnoreCase("string") == 0) {
			return "String";
		} else if (type.compareToIgnoreCase("dateTime") == 0) {
			return "java.util.Date";
		} else {
			return null;
		}
	}

	class Writer {
		FileWriter out = null;
		String lineSep = "\n";

		public Writer(String path) {
			try {
				out = new FileWriter(path);
			} catch (IOException e) {
			}
		}

		public void println(String line) {
			try {
				out.write(line + lineSep);
			} catch (IOException e) {
			}
		}

		public void close() {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		// if(args.length<3){
		// System.out.println("需要3个参数：设备模板全路径    生成类文件的路径    类名");
		// System.exit(0);
		// }
		JRDBSet.getInstance().attachAllDB();

		PluginGenerator generator = new PluginGenerator();
		// generator.generate(args[0],args[1],args[2]);

		generator.generate("D:/工作/软件平台部/03 项目情况/39 云端平台/SVN/曲阳光伏/doc/device/model/箱变设备.xml", "d:/","BoxTransformer");

	}
}
