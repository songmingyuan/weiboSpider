package com.cmcc.wltx.collector.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.json.JSONObject;

import com.cmcc.wltx.collector.service.IrocketMqconsumer;
import com.cmcc.wltx.collector.spider.util.Producer;
import com.cmcc.wltx.utils.MQPropertiesLoader;

public class RocketMqConsumerImpl implements IrocketMqconsumer {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RocketMqConsumerImpl.class);
	private DefaultMQPushConsumer consumer = null;
	private static int initialState = 0;
	private static final MQPropertiesLoader mqLoader = new MQPropertiesLoader();
	private String producerAddr = mqLoader.getPropertieParams("mqAddr");
	private String offset = "offset";

	public static void setInitialState(int initialState) {
		RocketMqConsumerImpl.initialState = initialState;
	}

	@Override
	public DefaultMQPushConsumer consumer(String mqConName) {
		try {
			if (initialState == 0 || consumer == null) {
				consumer = new DefaultMQPushConsumer(mqConName);
				logger.info("mq地址 " + producerAddr);
				consumer.setNamesrvAddr(producerAddr);
				consumer.setVipChannelEnabled(false);
				consumer.setInstanceName(UUID.randomUUID().toString());
				consumer.setConsumeMessageBatchMaxSize(1);
				consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
				RocketMqConsumerImpl.setInitialState(1);
			}
		} catch (Exception e) {
			logger.error("获取mq消费异常", e);
		}
		return consumer;
	}

	@Override
	public DefaultMQPushConsumer getconsumer(String mqConName, DefaultMQPushConsumer consumer) {
		try {
			if (initialState == 0) {
				logger.info("获取mq消费服务");
				logger.info("mq地址 " + producerAddr);
				RocketMqConsumerImpl.setInitialState(3);
			}
			consumer = new DefaultMQPushConsumer(mqConName);
			consumer.setNamesrvAddr(producerAddr);
			consumer.setVipChannelEnabled(false);
			consumer.setInstanceName(UUID.randomUUID().toString());
			consumer.setConsumeMessageBatchMaxSize(1);
			consumer.setPullInterval(5);
			consumer.setConsumeThreadMin(4);
			consumer.setConsumeThreadMax(4);
			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		} catch (Exception e) {
			logger.error("获取mq消费异常", e);
		}
		return consumer;
	}

	@Override
	/**
	 * 获取队列信息
	 */
	public List<String> getMessage(String topicName, String consumerName) {

		logger.info("开始拉取队列" + topicName + "数据");
		List<String> messageList = new ArrayList<>();
		DefaultMQPullConsumer consumerList = new DefaultMQPullConsumer();
		try {
			consumerList.setConsumerGroup(consumerName);
			logger.info("mq地址 " + producerAddr);
			consumerList.setNamesrvAddr(producerAddr);
			consumerList.setVipChannelEnabled(false);
			consumerList.start();
			Set<MessageQueue> mqs = consumerList.fetchSubscribeMessageQueues(topicName);
			for (MessageQueue mq : mqs) {
				setList(consumerList, mq, messageList);
			}
		} catch (Exception e) {
			logger.error("获取mq消费异常", e);
			if (e.getMessage().indexOf("Can not find Message Queue for this topic") != -1) {
				// 创建 此队列
				JSONObject jsonObject = new JSONObject();
				try {
					Producer.sendMessage(jsonObject, topicName, "", "", "con");
				} catch (Exception e1) {
					logger.error("获取mq消费异常", e1);
				}
			}
		} finally {
			consumerList.shutdown();
		}
		return messageList;
	}

	private void setList(DefaultMQPullConsumer consumerList, MessageQueue mq, List<String> messageList) {
		try {
			PullResult pullResult = consumerList.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 2);
			List<MessageExt> list = pullResult.getMsgFoundList();

			if (list != null && list.size() < 100) {
				for (MessageExt msg : list) {
					if (new String(msg.getBody()) != null && !new String(msg.getBody()).equals("")
							&& !new String(msg.getBody()).equals("null") && !new String(msg.getBody()).equals("{}")) {
						messageList.add(new String(msg.getBody()));
					}

				}
			}
			putMessageQueueOffset(mq, pullResult.getNextBeginOffset());

		} catch (Exception e) {
			logger.error("获取mq消费异常", e);
		}
	}

	private void putMessageQueueOffset(MessageQueue mq, long offset) {
		// JedisUtils.hset(mq.toString(),this.offset,offset+"");
	}

	private long getMessageQueueOffset(MessageQueue mq) {
		// if(JedisUtils.hget(mq.toString(),offset)!=null) {
		// return Long.parseLong(JedisUtils.hget(mq.toString(),"offset"));
		// }

		return 0;
	}

}
