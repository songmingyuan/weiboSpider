package com.cmcc.wltx.collector.spider.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;

import com.alibaba.fastjson.JSONObject;
import com.cmcc.wltx.database.JedisUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class TaskUtils {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TaskUtils.class);

	/**
	 * 获取分级队列的微博账户
	 * 
	 * @param limit
	 * @param taskTotals
	 * @param weights
	 * @return
	 */
	public static int[] countPops(int limit, double[] taskTotals, double[] weights) {
		int[] pops = new int[taskTotals.length];
		double[] proportions = new double[pops.length];
		for (int i = 0; i < pops.length; i++) {
			if (taskTotals[i] == 0) {
				continue;
			} else {
				pops[i] = -1;
				proportions[i] = 1;
				for (int j = 0; j < i; j++) {
					if (pops[j] != 0) {
						proportions[j] += weights[i] / weights[j] * taskTotals[i] / taskTotals[j];
					}
				}
			}
		}
		for (int i = 0; i < pops.length; i++) {
			if (pops[i] == 0) {
				continue;
			}
			pops[i] = (int) (limit / proportions[i]);
			limit -= pops[i];
		}
		return pops;
	}

	/**
	 * 获取有效的付费代理，redis集群
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getValidPayProxy_old() {
		Map<String, Object> resultMap = new HashMap<>();
		HttpHost proxy = null;
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = JedisUtils.jc;
			if (null != jedisCluster) {
				boolean flag = true;
				long timeInvalid = 0;
				do {
					String proxyStr = jedisCluster.rpop(JedisUtils.proxyKey);
					if (null == proxyStr) {
						logger.info("the redis key is not exits -- ", JedisUtils.proxyKey);
						break;
					}
					Map<String, String> proxyMap = JSONObject.parseObject(proxyStr, Map.class);
					if (null != proxyMap && proxyMap.size() == 4) {
						String host = proxyMap.get("host");
						int port = Integer.valueOf(proxyMap.get("port"));
						timeInvalid = Long.valueOf(proxyMap.get("invalidTime"));// 失效时间
						long nowTime = (System.currentTimeMillis()) / 1000;// 秒
						if (timeInvalid > nowTime) {
							// 失效期大于当前时间
							if (timeInvalid - nowTime > 10) {
								jedisCluster.lpush(JedisUtils.proxyKey, proxyStr);
							}
							proxy = new HttpHost(host, port);
							flag = false;
						}
					}
				} while (flag);
				if (proxy != null) {
					resultMap.put("invalidTime", timeInvalid);
					resultMap.put("proxy", proxy);
				}
			} else {
				logger.info("getValidPayProxy the JedisCluster find error");
			}
		} catch (Exception e) {
			logger.info("getValidPayProxy get proxy is error", e);
		}
		return resultMap;
	}

	/**
	 * 获取有效的付费代理，redis单节点
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getValidPayProxy() {
		Map<String, Object> resultMap = new HashMap<>();
		HttpHost proxy = null;
		Jedis jedis = null;
		try {
			jedis = JedisUtils.createJedis();
			if (null != jedis) {
				boolean flag = true;
				long timeInvalid = 0;
				do {
					String proxyStr = jedis.rpop(JedisUtils.proxyKey);
					if (null == proxyStr) {
						logger.error("the redis key is not exits -- ", JedisUtils.proxyKey);
						break;
					}
					Map<String, String> proxyMap = JSONObject.parseObject(proxyStr, Map.class);
					if (null != proxyMap && proxyMap.size() == 4) {
						String host = proxyMap.get("host");
						int port = Integer.valueOf(proxyMap.get("port"));
						timeInvalid = Long.valueOf(proxyMap.get("invalidTime"));// 失效时间
						long nowTime = (System.currentTimeMillis()) / 1000;
						if (timeInvalid > nowTime) {
							// 失效期大于当前时间
							if (timeInvalid - nowTime > 10) {
								jedis.lpush(JedisUtils.proxyKey, proxyStr);
							}
							proxy = new HttpHost(host, port);
							flag = false;
						}
					}
				} while (flag);
				if (proxy != null) {
					resultMap.put("invalidTime", timeInvalid);
					resultMap.put("proxy", proxy);
				}
			}
		} catch (Exception e) {
			logger.error("getValidPayProxy get proxy failed - ", e);
		} finally {
			if (null != jedis) {
				jedis.close();
			}
		}
		return resultMap;
	}

	public static String getMapValue(Map<String, Object> map, String param) {
		String paramValue = null;
		if (null != map && StringUtils.isNotBlank(param) && map.containsKey(param)) {
			paramValue = map.get(param).toString();
		}
		return paramValue;
	}
}
