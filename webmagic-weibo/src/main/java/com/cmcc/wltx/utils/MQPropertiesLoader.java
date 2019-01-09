package com.cmcc.wltx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * MQ配置文件加载
 * 
 * @author mingyuan.song
 *
 */
public class MQPropertiesLoader {
	private static Properties properties = new Properties();

	public MQPropertiesLoader() {
		super();
	}

	static {
		init();
	}

	private static void init() {
		File propFile = new File(System.getProperty("user.dir") + File.separatorChar + "mq.properties");
		if (!propFile.isFile()) {
			propFile = new File(propFile.getParentFile().getParentFile(), "mq.properties");
		}
		if (!propFile.isFile()) {
			throw new Error("MQ配置文件不存在");
		}
		try {
			FileInputStream in = new FileInputStream(propFile);
			properties.load(in);
			in.close();
		} catch (Exception e) {
			throw new Error("MQ始化错误", e);
		}
	}

	public String getPropertieParams(String param) {
		String paramValue = null;
		if (null != properties.getProperty(param)) {
			paramValue = properties.getProperty(param);
		}
		return paramValue;
	}
}
