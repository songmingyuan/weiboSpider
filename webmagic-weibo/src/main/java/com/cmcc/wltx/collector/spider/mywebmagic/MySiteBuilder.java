package com.cmcc.wltx.collector.spider.mywebmagic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpHost;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;
import com.cmcc.wltx.database.JedisUtils;

public class MySiteBuilder {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MySiteBuilder.class);

	private static final String FILENAME_CONFIGURATION = "configuration.xml";
	private static final String FILEPATHNAME_CONFIGURATION = ConstantsHome.USER_DIR + File.separatorChar
			+ FILENAME_CONFIGURATION;

	public static MySite buildMySite()
			throws ParserConfigurationException, SAXException, IOException, ConfigurationException {
		MySite mySite = new MySite();
		Document doc = parseConfiguration(FILEPATHNAME_CONFIGURATION);
		XPath xpath = XPathFactory.newInstance().newXPath();
		configMySite(mySite, doc, xpath);
		return mySite;
	}

	private static void configMySite(MySite mySite, Document doc, XPath xpath) throws ConfigurationException {
		try {
			// pipeline
			String filePipelinePath = xpath.evaluate("/config/pipeline/filepipeline/text()", doc).trim();
			if (filePipelinePath.length() != 0) {
				mySite.setFilePipelinePath(filePipelinePath);
			}

			// uuid
			String uuid = xpath.evaluate("/config/site/@uuid", doc).trim();
			if (uuid.length() != 0) {
				mySite.setUuid(uuid);
			}

			// 业务类型
			String businessType = xpath.evaluate("/config/site/businessType/text()", doc).trim();
			if (businessType.length() != 0) {
				mySite.setBusinessType(businessType);
			}

			// domain
			String domain = xpath.evaluate("/config/site/domain/text()", doc).trim();
			if (domain.length() != 0) {
				mySite.setDomain(domain);
			}

			// charset
			String charset = xpath.evaluate("/config/site/charset/text()", doc).trim();
			if (charset.length() != 0) {
				mySite.setCharset(charset);
			}

			// 重试次数
			String retryTimes = xpath.evaluate("/config/site/retrytimes/text()", doc).trim();
			if (retryTimes.length() != 0) {
				try {
					mySite.setRetryTimes(Integer.parseInt(retryTimes));
				} catch (NumberFormatException e) {
					logger.warn("retrytimes非整数", e);
				}
			}

			// 请求间隔
			String sleepTime = xpath.evaluate("/config/site/sleeptime/text()", doc).trim();
			if (sleepTime.length() != 0) {
				try {
					mySite.setSleepTime(Integer.parseInt(sleepTime));
				} catch (NumberFormatException e) {
					logger.warn("sleeptime非整数", e);
				}
			}

			// 信源类型
			String recType = xpath.evaluate("/config/site/recType/text()", doc).trim();
			if (recType.length() != 0) {
				try {
					mySite.setRecType(Integer.parseInt(recType));
				} catch (NumberFormatException e) {
					logger.warn("recType非整数", e);
				}
			}

			// 请求失败时的间隔
			String retrySleepTime = xpath.evaluate("/config/site/retrysleeptime/text()", doc).trim();
			if (retrySleepTime.length() != 0) {
				try {
					mySite.setRetrySleepTime(Integer.parseInt(retrySleepTime));
				} catch (NumberFormatException e) {
					logger.warn("retrysleeptime非整数", e);
				}
			}

			// 请求超时时间
			String timeout = xpath.evaluate("/config/site/timeout/text()", doc).trim();
			if (timeout.length() != 0) {
				try {
					mySite.setTimeOut(Integer.parseInt(timeout));
				} catch (NumberFormatException e) {
					logger.warn("timeout非整数", e);
				}
			}

			// 轮询间隔
			String timeinterval = xpath.evaluate("/config/site/timeinterval/text()", doc).trim();
			if (timeinterval.length() != 0) {
				try {
					mySite.setTimeInterval(Integer.parseInt(timeinterval));
				} catch (NumberFormatException e) {
					logger.warn("timeinterval非整数", e);
				}
			}

			// 线程数量
			String threadnum = xpath.evaluate("/config/site/threadnum/text()", doc).trim();
			if (threadnum.length() != 0) {
				try {
					mySite.setThreadNum(Integer.parseInt(threadnum));
				} catch (NumberFormatException e) {
					logger.warn("threadnum非整数", e);
				}
			}

			// 代理
			String host = xpath.evaluate("/config/site/proxy/@host", doc).trim();
			String port = xpath.evaluate("/config/site/proxy/@port", doc).trim();
			if (host.length() != 0 && port.length() != 0) {
				try {
					mySite.setHttpProxy(new HttpHost(host, Integer.parseInt(port)));
				} catch (NumberFormatException e) {
					logger.warn("proxy/port非整数", e);
				}
			}

			// redis
			String redisHost = xpath.evaluate("/config/redis/host/text()", doc).trim();
			String redisPort = xpath.evaluate("/config/redis/port/text()", doc).trim();
			if (redisHost.length() != 0 && redisPort.length() != 0) {
				try {
					mySite.setRedisPort(Integer.parseInt(redisPort));
					mySite.setRedisHost(redisHost);
				} catch (NumberFormatException e) {
					logger.warn("/config/redis/port非整数", e);
				}
			}
			String redisQueueKey = xpath.evaluate("/config/redis/keys/queue/text()", doc).trim();
			if (redisQueueKey.length() != 0) {
				mySite.setTargetTaskQueueRedisKeyPrefix(redisQueueKey);
			}
			String redisSetKey = xpath.evaluate("/config/redis/keys/zset/text()", doc).trim();
			if (redisSetKey.length() != 0) {
				JedisUtils.zsetKey = redisSetKey;
			}
		} catch (XPathExpressionException e) {
			throw new Error(e);
		}
	}

	private static Document parseConfiguration(String filePathName)
			throws ParserConfigurationException, SAXException, IOException {
		File configFile = new File(filePathName);
		if (!configFile.exists()) {
			throw new FileNotFoundException(configFile.getAbsolutePath());
		}
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(configFile);
		return doc;
	}

	public static MySite buildMyTemplateSite()
			throws ParserConfigurationException, SAXException, IOException, ConfigurationException {
		MySite site = new MySite();
		Document doc = parseConfiguration(FILEPATHNAME_CONFIGURATION);
		XPath xpath = XPathFactory.newInstance().newXPath();
		configMyTemplateSite(site, doc, xpath);
		return site;
	}

	private static void configMyTemplateSite(MySite site, Document doc, XPath xpath) throws ConfigurationException {
		configMySite(site, doc, xpath);
		try {
			// 模板存放路径
			String templatesPath = xpath.evaluate("/config/site/templatesPath/text()", doc);
			if (templatesPath.length() != 0) {
				site.setTemplatesPath(templatesPath.trim());
			}
		} catch (XPathExpressionException e) {
			throw new Error(e);
		}
	}

}
