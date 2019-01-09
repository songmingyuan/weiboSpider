package com.cmcc.wltx.collector.pageprocessor.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.exception.PageProcessException;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.scheduler.QueueScheduler;
import com.cmcc.wltx.collector.service.PayProxyTaskService;
import com.cmcc.wltx.collector.service.ServiceFactory;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.downloader.MyHttpClientDownloader;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.MyNullPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;
import com.cmcc.wltx.database.JedisUtils;

import net.minidev.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;

public class Pay_ProxyPageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Pay_ProxyPageProcessor.class);
	private final PayProxyTaskService payProxyTaskService;

	public Pay_ProxyPageProcessor(MySite site) {
		super(site);
		this.payProxyTaskService = ServiceFactory.getPayProxyTaskService();
	}

	/**
	 * 使用redis集群，
	 * 
	 * @param page
	 * @throws PageProcessException
	 */
	public void process_old(Page page) throws PageProcessException {
		Request currentRequest = page.getRequest();
		String rawText = page.getRawText();
		if (null == rawText || rawText.length() == 0) {
			logger.error("rawText isEmpty - {}", currentRequest.getUrl());
			return;
		}
		List<Map<String, String>> proxyList = analysisRawText(rawText);
		if (proxyList.size() > 0) {
			logger.info("本次获取的付费代理数为 -- {}", proxyList.size());
			try {
				// 入库备份
				payProxyTaskService.createProxyDataList(proxyList);
				logger.info("t_config_pay_proxy 代理入库成功");
			} catch (ServiceException e) {
				logger.error("add pay proxy error", e);
			}
			try {
				// 加入redis
				JedisCluster jedisCluster = JedisUtils.jc;
				if (null != jedisCluster) {
					for (Map<String, String> detail : proxyList) {
						String proxyStr = JSONObject.toJSONString(detail);
						jedisCluster.lpush(JedisUtils.proxyKey, proxyStr);
					}
					logger.info("add pay proxy to redisCluster success");
				}
			} catch (Exception e) {
				throw new PageProcessException(e);
			}
		}
	}

	/**
	 * 使用redis单节点
	 * 
	 * @param page
	 * @throws PageProcessException
	 */
	@Override
	public void process(Page page) throws PageProcessException {
		Request currentRequest = page.getRequest();
		String rawText = page.getRawText();
		if (null == rawText || rawText.length() == 0) {
			logger.error("rawText isEmpty - {}", currentRequest.getUrl());
			return;
		}
		List<Map<String, String>> proxyList = analysisRawText(rawText);
		if (proxyList.size() > 0) {
			logger.info("本次获取的付费代理数为 -- {}", proxyList.size());
			try {
				// 入库备份
				payProxyTaskService.createProxyDataList(proxyList);
				logger.info("t_config_pay_proxy 代理入库成功");
			} catch (ServiceException e) {
				logger.error("add pay proxy error", e);
			}
			// 加入redis
			Jedis jedis = null;
			try {
				jedis = JedisUtils.createJedis();
				if (null != jedis) {
					String[] proxyStr = new String[proxyList.size()];
					for (int num = 0; num < proxyList.size(); num++) {
						proxyStr[num] = JSONObject.toJSONString(proxyList.get(num));
					}
					Pipeline p = jedis.pipelined();
					p.lpush(JedisUtils.proxyKey, proxyStr);
					p.sync();
					logger.info("add pay proxy to redis success");
				}
			} catch (Exception e) {
				throw new PageProcessException(e);
			} finally {
				if (null != jedis) {
					jedis.close();
				}
			}
		}
	}

	public List<Map<String, String>> analysisRawText(String rawText) {
		List<Map<String, String>> proxyList = new ArrayList<>();
		String[] spitStr = rawText.split("\r\n");// 按行解析
		if (spitStr != null && spitStr.length > 0) {
			for (String detail : spitStr) {
				Map<String, String> detailMap = new HashMap<>();
				String[] detailStr = detail.split(",");// 按逗号解析
				if (detailStr != null && detailStr.length == 5) {
					String[] proxyStr = detailStr[0].split(":");// 按分号解析
					if (proxyStr == null || proxyStr.length != 2) {
						continue;
					}
					detailMap.put("host", proxyStr[0]);// ip
					detailMap.put("port", proxyStr[1]);// 端口
					detailMap.put("creatTime", detailStr[3]);// 入库时间
					detailMap.put("invalidTime", detailStr[4]);// 失效时间
					proxyList.add(detailMap);
				}
			}
		}
		return proxyList;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			logger.error("缺少爬虫ID");
			return;
		}
		MyDataSource.init();
		try {
			launch(args);
		} finally {
			MyDataSource.destroy();
		}
	}

	private static void launch(String[] spiderIds) {
		String[] newSpiderIds = new String[1];
		String spiderId = spiderIds[0];
		// 0：本机ip；1：内部代理；2：免费外部代理；3：付费外部代理；
		int proxyType = 0;
		if (spiderIds.length == 1) {

		} else if (spiderIds.length == 2) {
			String proxyTypeId = spiderIds[1];
			if (StringUtils.isNotBlank(proxyTypeId)) {
				proxyType = Integer.valueOf(proxyTypeId);
			}
		} else {
			logger.error("args number is error");
			return;
		}
		newSpiderIds[0] = spiderId;
		MySpider.test = spiderId.startsWith(MySpider.PREFIX_ID_TEST);
		// 获取配置
		SpiderService spiderService = ServiceFactory.getSpiderService();
		MySite site;
		try {
			site = spiderService.buildMySiteNew(newSpiderIds);
		} catch (ServiceException e) {
			logger.error("spider config load error", e);
			return;
		}
		if (MySpider.test) {
			// 初始化redis
			String redisHost = site.getRedisHost();
			int redisPort = site.getRedisPort();
			if (null == redisHost || redisHost.length() == 0 || redisPort <= 0) {
				logger.error("redis config error");
				return;
			}
			JedisUtils.initPool(redisHost, redisPort);
		} else {
			JedisUtils.initPoolBySentinel();
		}

		MySpider spider = null;
		try {
			// 创建爬虫
			spider = MySpider.create(new Pay_ProxyPageProcessor(site), null, new QueueScheduler(), proxyType,
					new MyNullPipeline());
			do {
				// 采集新代理
				Request request = new Request("http://dly.134t.com/query.txt?key=NP3EBB116F&detail=true&count=1000",
						true);
				Map<String, String> requestHeaders = new HashMap<String, String>();
				requestHeaders.put("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.119 Safari/537.36");
				requestHeaders.put("Host", "dly.134t.com");
				requestHeaders.put("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
				requestHeaders.put("Accept-Encoding", "gzip, deflate");
				requestHeaders.put("Accept-Language", "zh-CN,zh;q=0.9");
				requestHeaders.put("Cache-Control", "no-cache");
				requestHeaders.put("Connection", "keep-alive");
				requestHeaders.put("Cookie", "service=49729DEF931FD763DEAB7D8675A32066");
				requestHeaders.put("Pragma", "no-cache");
				requestHeaders.put("Upgrade-Insecure-Requests", "1");
				request.putExtra(ConstantsHome.REQUEST_EXTRA_HEADERS, requestHeaders);
				logger.info("start task - {}", request.getUrl());
				spider.addRequest(request);

				spider.run();

				if (ConstantsHome.BLOCK.exists()) {
					break;
				}

				if (site.getTimeInterval() > 0) {
					logger.info("interval sleeping...");
					try {
						Thread.sleep(site.getTimeInterval());
					} catch (InterruptedException e) {
						logger.info("interval sleep interrupted", e);
					}
					if (ConstantsHome.BLOCK.exists()) {
						break;
					}
				}
				if (proxyType == 2) {
					((MyHttpClientDownloader) spider.getDownloader()).initProxy(proxyType);
				}
			} while (true);
		} finally {
			try {
				if (null != spider) {
					spider.close();
				}
			} finally {
				JedisUtils.closePool();
			}
		}
	}
}
