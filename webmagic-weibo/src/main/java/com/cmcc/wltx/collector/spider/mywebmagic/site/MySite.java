package com.cmcc.wltx.collector.spider.mywebmagic.site;

import java.io.File;

import us.codecraft.webmagic.Site;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.model.Article;

public class MySite extends Site {
	private String targetTaskQueueRedisKeyPrefix = "queue_task_target_";
	private String ips;
	private int taskLimit = 2000;
	private int recType = Article.RECTYPE_NEWS;
	private String uuid = "target";
	private String name = "新闻";
	private int timeInterval = 0;
	private int threadNum = 1;
	private String filePipelinePath = ConstantsHome.USER_DIR + File.separator + "out";
	private String redisHost = null;
	private int redisPort = -1;
	private String templatesPath = ConstantsHome.DEFAULT_TEMPLATES_PATH;
	private String businessType;
	private boolean storeCookie = true;
	private int level = 0;// 商机爬虫的级别
	private int proxyId = 0;// 商机爬虫的代理级别

	public MySite() {
		super();
		// 初始化默认值
		setRetryTimes(3);
		setSleepTime(1000);
		setRetrySleepTime(3000);
		setTimeOut(10000);
		setCycleRetryTimes(3);
	}

	public MySite(int timesNum) {
		super();
		// 初始化默认值
		setRetryTimes(3);
		setSleepTime(1000);
		setRetrySleepTime(3000);
		setTimeOut(10000);
		setCycleRetryTimes(timesNum);
	}

	public void setCookie(String cookie) {
		String[] nvPairs = cookie.split("; ");
		for (String nvPair : nvPairs) {
			String[] nv = nvPair.split("=");
			addCookie(nv[0], nv.length == 2 ? nv[1] : "");
		}
	}

	public int getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilePipelinePath() {
		return filePipelinePath;
	}

	public void setFilePipelinePath(String filePipelinePath) {
		this.filePipelinePath = filePipelinePath;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public int getRecType() {
		return recType;
	}

	public void setRecType(int recType) {
		this.recType = recType;
	}

	public String getTemplatesPath() {
		return templatesPath;
	}

	public void setTemplatesPath(String templatesPath) {
		this.templatesPath = templatesPath;
	}

	public int getTaskLimit() {
		return taskLimit;
	}

	public void setTaskLimit(int taskLimit) {
		this.taskLimit = taskLimit;
	}

	public String getIps() {
		return ips;
	}

	public void setIps(String ips) {
		this.ips = ips;
	}

	public String getTargetTaskQueueRedisKeyPrefix() {
		return targetTaskQueueRedisKeyPrefix;
	}

	public void setTargetTaskQueueRedisKeyPrefix(String targetTaskQueueRedisKeyPrefix) {
		this.targetTaskQueueRedisKeyPrefix = targetTaskQueueRedisKeyPrefix;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public boolean isStoreCookie() {
		return storeCookie;
	}

	public void setStoreCookie(boolean storeCookie) {
		this.storeCookie = storeCookie;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getProxyId() {
		return proxyId;
	}

	public void setProxyId(int proxyId) {
		this.proxyId = proxyId;
	}

}
