package com.cmcc.wltx.collector.service;

import java.util.List;

import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.FollowedWeiboSpiderTask;

public interface FollowedWeiboSpiderTaskService {
	List<FollowedWeiboSpiderTask> tasksForCrawl(String spiderId, int limit) throws ServiceException;
	List<FollowedWeiboSpiderTask> getTasksBySpiderId(String spiderId) throws ServiceException;
	void releaseTasks(String spiderId) throws ServiceException;
	void removeInvalidCookie(Long taskId) throws ServiceException;
}
