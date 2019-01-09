package com.cmcc.wltx.collector.service;

import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public interface SpiderService {
	MySite buildMySite(String[] spiderIds) throws ServiceException;

	MySite buildMySiteNew(String[] spiderIds) throws ServiceException;

	boolean updateSpiderStatus(String spiderId, int status) throws ServiceException;
}
