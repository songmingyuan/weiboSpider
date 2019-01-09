package com.cmcc.wltx.collector.spider.mywebmagic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;

import com.cmcc.wltx.collector.constraint.UniqueConstraint;
import com.cmcc.wltx.collector.exception.LogicError;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.pageprocessor.aspect.PageProcessorProxy;
import com.cmcc.wltx.collector.spider.mywebmagic.downloader.MyHttpClientDownloader;
import com.cmcc.wltx.collector.spider.mywebmagic.scheduler.QueuesScheduler;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public class MySpider extends Spider {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MySpider.class);
	public static final String PREFIX_ID_TEST = "test_";
	public static boolean test = false;
	private UniqueConstraint uniqueConstraint;

	private MySpider(PageProcessor pageProcessor) {
		this(pageProcessor, false);
	}

	private MySpider(PageProcessor pageProcessor, boolean destroyWhenExit) {
		super(pageProcessor);
		super.destroyWhenExit = destroyWhenExit;
	}

	public static MySpider create(BasicPageProcessor pageProcessor) {
		MySpider mySpider = new MySpider(new PageProcessorProxy(pageProcessor));
		pageProcessor.setMySpider(mySpider);
		return mySpider;
	}

	public static MySpider create(BasicPageProcessor pageProcessor, UniqueConstraint uc, Scheduler scheduler,
			int proxyType, Pipeline... pipelines) {
		MySpider mySpider = new MySpider(new PageProcessorProxy(pageProcessor));
		MySite mySite = pageProcessor.getMySite();
		mySpider.setUUID(mySite.getUuid());
		mySpider.thread(mySite.getThreadNum());

		List<SpiderListener> spiderListeners = new ArrayList<SpiderListener>(1);
		spiderListeners.add(pageProcessor);
		mySpider.setSpiderListeners(spiderListeners);
		mySpider.setDownloader(new MyHttpClientDownloader(mySite.getIps(), proxyType));

		if (null != uc) {
			mySpider.setUniqueConstraint(uc);
		}
		if (null != scheduler) {
			mySpider.setScheduler(scheduler);
		}
		if (null != pipelines) {
			mySpider.setPipelines(Arrays.asList(pipelines));
		}
		pageProcessor.setMySpider(mySpider);
		return mySpider;
	}

	public Downloader getDownloader() {
		return this.downloader;
	}

	public MySite getMySite() {
		return (MySite) getSite();
	}

	public Spider addUrl(boolean reusable, String... urls) {
		Request[] reqs = new Request[urls.length];
		for (int i = 0; i < reqs.length; i++) {
			reqs[i] = new Request(urls[i], reusable);
		}
		addRequest(reqs);
		return this;
	}

	@Override
	public void run() {
		try {
			super.run();
		} catch (Throwable e) {
			throw e;
		} finally {
			logger.info("Spider " + getUUID() + " stopped!");
		}
	}

	public boolean pause() {
		if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
			return true;
		} else {
			return false;
		}
	}

	public void block() {
		stat.set(Status.Blocked.getValue());
	}

	public void unblock() {
		stat.set(Status.Stopped.getValue());
	}

	public PageProcessor getPageProcessor() {
		return pageProcessor;
	}

	public void setStatusCode(int status) {
		stat.set(status);
	}

	public int getStatusCode() {
		return stat.get();
	}

	public void resetUrl() {
		if (!(scheduler instanceof QueuesScheduler)) {
			throw new LogicError("暂未处理的scheduler - " + scheduler);
		}
		QueuesScheduler mqs = (QueuesScheduler) scheduler;
		mqs.clear();
		mqs.getDuplicateRemover().resetDuplicateCheck(this);
	}

	public int pushUrl(String url, boolean reusable) {
		return scheduler.push(new Request(url, reusable), this);
	}

	public void setUniqueConstraint(UniqueConstraint uniqueConstraint) {
		this.uniqueConstraint = uniqueConstraint;
	}

	public boolean isUnique(String id) {
		return uniqueConstraint.check(id);
	}

	// 美团商家数据MD5(meituan_cityname_hotelname)作为redis的key
	public boolean isOnlyKey(String key) {
		return uniqueConstraint.inspect(key);
	}

	public boolean isUniqueBySJ(String key) {
		return uniqueConstraint.checkSJ(key);
	}
}
