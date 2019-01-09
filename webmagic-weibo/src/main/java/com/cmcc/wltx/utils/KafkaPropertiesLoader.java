package com.cmcc.wltx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import kafka.producer.ProducerConfig;

/**
 * kafka配置文件加载
 * 
 * @author mingyuan.song
 *
 */
public class KafkaPropertiesLoader {
	private static Properties properties = new Properties();
	private ProducerConfig config;
	private static String topic;

	public KafkaPropertiesLoader() {
		super();
	}

	static {
		init();
	}

	private static void init() {
		File propFile = new File(System.getProperty("user.dir") + File.separatorChar + "kafka.properties");
		if (!propFile.isFile()) {
			propFile = new File(propFile.getParentFile().getParentFile(), "kafka.properties");
		}
		if (!propFile.isFile()) {
			throw new Error("kafka配置文件不存在");
		}
		try {
			FileInputStream in = new FileInputStream(propFile);
			properties.load(in);
			in.close();
			topic = properties.getProperty("topic");
		} catch (Exception e) {
			throw new Error("kafka始化错误", e);
		}
	}

	public ProducerConfig getConfig() {
		// 加载producer配置
		if (null == config) {
			config = new ProducerConfig(properties);
		}
		return config;
	}

	public String getTopic() {
		return topic;
	}
}
