package com.cmcc.wltx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
	private static Properties properties = new Properties();

	static{
		init();
	}

	private static void init() {
//		String classpath = PropertiesLoader.class.getResource("/").getFile();
//		File dir = new File(classpath);
//		File[] files = dir.listFiles(new FilenameFilter() {
//			@Override
//			public boolean accept(File dir, String name) {
//				return name.endsWith(".properties");
//			}
//		});
//		
//		for (File file : files) {
//			try {
//				properties.load(new FileInputStream(file));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		InputStream in = PropertiesLoader.class.getResourceAsStream("/jdbc.properties");
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getString(String key,String defaultValue){
		return properties.getProperty(key, defaultValue);
	}
	
	public static String getString(String key){
		return properties.getProperty(key);
	}
	
	public static Integer getInteger(String key){
		String result = getString(key);
		try{
			return Integer.valueOf(result);
		}catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) {
	}
}
