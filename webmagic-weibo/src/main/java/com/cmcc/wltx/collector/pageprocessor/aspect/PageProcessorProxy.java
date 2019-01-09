package com.cmcc.wltx.collector.pageprocessor.aspect;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.pageprocessor.weibo.HomeWeiboPageProcessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.processor.PageProcessor;

public class PageProcessorProxy implements PageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PageProcessorProxy.class);
	private final org.slf4j.Logger LOGGER_TARGET;

	private PageProcessor pageProcessor;

	public PageProcessorProxy(PageProcessor pageProcessor) {
		this.pageProcessor = pageProcessor;
		if (pageProcessor instanceof HomeWeiboPageProcessor) {
			LOGGER_TARGET = org.slf4j.LoggerFactory.getLogger("com.cmcc.wltx.collector.pageprocessor");
		} else {
			LOGGER_TARGET = null;
		}
	}

	@Override
	public void process(Page page) {
		page.setSkip(true);
		String currentUrl = page.getUrl().toString();
		logger.info("process start - {}", currentUrl);
		try {
			this.pageProcessor.process(page);
			logger.info("process done - {}", currentUrl);
		} catch (PageProcessException e) {
			handlePageProcessException(page, e);
		}

	}

	/**
	 * 处理异常
	 * 
	 * @param page
	 * @param e
	 */
	private void handlePageProcessException(Page page, PageProcessException e) {
		Request request = page.getRequest();
		if (null != LOGGER_TARGET) {
			String siteTaskId = (String) request.getExtra(ConstantsHome.REQUEST_EXTRA_SITE_TASK_ID);
			if (null != siteTaskId) {
				LOGGER_TARGET.info("{}\t{}\t{}\t{}\t{}\t{}", siteTaskId, 0, 0, 0, 0, 1);
			}
		}
		logger.warn(request.toString(), e);
		String msg = e.getMessage();
		if (null == msg || !msg.endsWith(BasicPageProcessor.SUFFIX_REQUEST_ERROR)) {
			Page ep = e.getPage();
			if (null == ep) {
				logger.warn(page.getRawText());
			} else {
				logger.warn(ep.getUrl().toString());
				logger.warn(ep.getRawText());
			}
		}
	}

	@Override
	public Site getSite() {
		return this.pageProcessor.getSite();
	}

	public PageProcessor getTarget() {
		return this.pageProcessor;
	}

}
