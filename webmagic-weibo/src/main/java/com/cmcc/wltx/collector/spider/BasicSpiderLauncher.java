package com.cmcc.wltx.collector.spider;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.service.ServiceFactory;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public class BasicSpiderLauncher {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BasicSpiderLauncher.class);

	public BasicSpiderLauncher() {
		super();
	}

	public static void launch(SpiderLauncher target, String[] spiderIds) {
		if (spiderIds.length == 0) {
			System.err.println("缺少爬虫ID");
			return;
		}
		MySpider.test = spiderIds[spiderIds.length - 1]
				.startsWith(MySpider.PREFIX_ID_TEST);

		MyDataSource.init();
		try {
			MySite site = loadMySite(spiderIds);
			target.launchSpider(site);
		} catch (Throwable e) {
			logger.error("异常退出", e);
		} finally {
			MyDataSource.destroy();
		}
	}

	private static MySite loadMySite(String[] spiderIds) {
		// 获取配置
		SpiderService spiderService = ServiceFactory.getSpiderService();
		MySite site;
		try {
			site = spiderService.buildMySite(spiderIds);
		} catch (ServiceException e) {
			throw new Error("spider config load error", e);
		}
		return site;
	}

}
