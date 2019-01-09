package com.cmcc.wltx.collector.spider.util;

import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.json.JSONObject;

import com.cmcc.wltx.collector.service.IrocketMqProducer;
import com.cmcc.wltx.collector.service.impl.RocketMqProducerImpl;
import com.cmcc.wltx.utils.MQPropertiesLoader;

public class Producer {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Producer.class);
	private static final MQPropertiesLoader mqLoader = new MQPropertiesLoader();
	// private static DefaultMQProducer producer = null;

	/**
	 * 发送消息
	 * 
	 * @param jsonObject
	 * @param topicName
	 * @param tag
	 * @param keys
	 * @param producerName
	 * @return
	 * @throws Exception
	 */
	public static SendResult sendMessage(JSONObject jsonObject, String topicName, String tag, String keys,
			String producerName) throws Exception {
		IrocketMqProducer irocketMqProducer = new RocketMqProducerImpl();
		DefaultMQProducer producer = irocketMqProducer
				.getcreateProducter(mqLoader.getPropertieParams("producer.mq.name"));
		try {
			// producer.start();
			logger.info("发送生产者消息:" + jsonObject.toString());
			Message msg = new Message(topicName, tag, keys, jsonObject.toString().getBytes());
			SendResult sendResult = producer.send(msg);
			logger.info("发送生产者消息成功");
			return sendResult;
		} catch (Exception e) {
			logger.error("发送消息异常：", e);
			throw e;
		} finally {
			Thread.sleep(200);
			producer.shutdown();
		}
	}

	/**
	 * 发送多条消息
	 * 
	 * @param messageList
	 * @param producerName
	 * @throws Exception
	 */
	public static void sendMessageList(List<Map<String, Object>> messageList, String producerName) throws Exception {
		IrocketMqProducer irocketMqProducer = new RocketMqProducerImpl();
		DefaultMQProducer producer = irocketMqProducer
				.getcreateProducter(mqLoader.getPropertieParams("producer.mq.name"));
		try {
			// producer.start();
			logger.info("批量发送生产者消息");
			// this.producerName=producerName;
			int num = 0;
			for (Map<String, Object> map : messageList) {
				num++;
				if (num > 200) {
					Thread.sleep(1000l * 60l * 1l);
					num = 0;
				}
				logger.info("批量发送生产者消息" + map);
				Message msg = new Message(TaskUtils.getMapValue(map, "topicName"), TaskUtils.getMapValue(map, "tag"),
						TaskUtils.getMapValue(map, "keys"), TaskUtils.getMapValue(map, "jsonObject").getBytes());
				SendResult sendResult = producer.send(msg);
				System.out.println(sendResult);
			}
			logger.info("发送生产者消息成功");
		} catch (Exception e) {
			logger.error("发送消息异常：", e);
			throw e;
		} finally {
			Thread.sleep(200);
			producer.shutdown();
		}

	}

	public Producer() {

	}

	public static void main(String[] args) throws Exception {

		DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
		producer.setNamesrvAddr("192.168.1.140:9876");
		producer.setVipChannelEnabled(false);
		producer.start();

		for (int i = 0; i < 5; i++) {
			try {
				Message msg = new Message("TopicTest", // topic
						"TagA", // tag
						("Hello RocketMQ " + i).getBytes()// body
				);
				SendResult sendResult = producer.send(msg);
				System.out.println(sendResult);
				Thread.sleep(6000);
			} catch (Exception e) {

				Thread.sleep(3000);
			}
		}

		producer.shutdown();
	}

}