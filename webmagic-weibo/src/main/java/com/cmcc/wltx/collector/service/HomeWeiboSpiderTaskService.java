package com.cmcc.wltx.collector.service;

import java.util.List;

import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.HomeWeiboSpiderTask;
import com.cmcc.wltx.collector.model.WeiboUser;

public interface HomeWeiboSpiderTaskService {
	List<HomeWeiboSpiderTask> tasksForSpider(String spiderId, int limit, double[] weights) throws ServiceException;

	List<HomeWeiboSpiderTask> tasksForSpiderNew(String spiderId, int limit) throws ServiceException;

	void releaseTasks(String spiderId) throws ServiceException;

	void transferLevel(WeiboUser user, int from, int to);

	void transferLevel_old(WeiboUser user, int from, int to);
}
