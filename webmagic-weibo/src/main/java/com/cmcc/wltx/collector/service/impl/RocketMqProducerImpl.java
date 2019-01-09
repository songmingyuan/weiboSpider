package com.cmcc.wltx.collector.service.impl;

import java.util.UUID;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import com.cmcc.wltx.collector.service.IrocketMqProducer;
import com.cmcc.wltx.utils.MQPropertiesLoader;

public class RocketMqProducerImpl implements IrocketMqProducer {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RocketMqProducerImpl.class);
	private static final MQPropertiesLoader mqLoader = new MQPropertiesLoader();
	private DefaultMQProducer producer = null;
	private int initialState = 0;

	@Override
	public DefaultMQProducer createProducter(String mq) {
		if (producer == null) {
			producer = new DefaultMQProducer(mq);
		}
		if (initialState == 0) {
			logger.info("获取mq生产者");
			String producerAddr = mqLoader.getPropertieParams("mqAddr");
			logger.info("生产服务地址为：" + producerAddr);
			producer.setNamesrvAddr(producerAddr);
			producer.setInstanceName(UUID.randomUUID().toString());
			producer.setVipChannelEnabled(false);
			try {
				producer.start();
			} catch (MQClientException e) {
				logger.error("异常信息为：", e);
				return null;
			}
			initialState = 1;
		}
		return producer;
	}

	public DefaultMQProducer getcreateProducter(String mq) {
		logger.info("获取mq生产者");
		String producerAddr = mqLoader.getPropertieParams("mqAddr");
		DefaultMQProducer producerp = new DefaultMQProducer(mq);
		producerp.setNamesrvAddr(producerAddr);
		producerp.setInstanceName(UUID.randomUUID().toString());
		producerp.setVipChannelEnabled(false);
		producerp.setRetryTimesWhenSendFailed(5);
		try {
			producerp.start();
			logger.info("生产服务地址为：" + producerAddr);
		} catch (MQClientException e) {
			logger.error("异常信息为：", e);
			return null;
		}
		return producerp;
	}
}
