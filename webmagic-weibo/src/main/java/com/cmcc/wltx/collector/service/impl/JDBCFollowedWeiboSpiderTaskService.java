package com.cmcc.wltx.collector.service.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.cmcc.wltx.collector.dao.DaoFactory;
import com.cmcc.wltx.collector.dao.FollowedWeiboSpiderTaskDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.FollowedWeiboSpiderTask;
import com.cmcc.wltx.collector.service.FollowedWeiboSpiderTaskService;

public class JDBCFollowedWeiboSpiderTaskService implements
		FollowedWeiboSpiderTaskService {
	private final FollowedWeiboSpiderTaskDao followedWeiboSpiderTaskDao = DaoFactory
			.getFollowedWeiboSpiderTaskDao();

	@Override
	public List<FollowedWeiboSpiderTask> tasksForCrawl(String spiderId,
			int limit) throws ServiceException {
		int res;
		try {
			res = followedWeiboSpiderTaskDao.updateForCrawl(spiderId, limit);
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("爬虫[" + spiderId + "]的任务更新异常", e);
		}

		if (res == 0) {
			return Collections.emptyList();
		}
		try {
			return followedWeiboSpiderTaskDao.listBySpiderId(spiderId);
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("爬虫[" + spiderId + "]的任务获取异常", e);
		}
	}

	@Override
	public void releaseTasks(String spiderId) throws ServiceException {
		try {
			followedWeiboSpiderTaskDao.releaseTasks(spiderId);
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("爬虫[" + spiderId + "]的任务释放异常", e);
		}
	}

	@Override
	public List<FollowedWeiboSpiderTask> getTasksBySpiderId(String spiderId)
			throws ServiceException {
		try {
			return followedWeiboSpiderTaskDao.specialListBySpiderId(spiderId);
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("爬虫[" + spiderId + "]的任务获取异常", e);
		}
	}

	@Override
	public void removeInvalidCookie(Long taskId) throws ServiceException {
		try {
			followedWeiboSpiderTaskDao.updateCookieById(taskId, "");
		} catch (SQLException e) {
			throw new ServiceException("任务[" + taskId + "]的cookie清除异常", e);
		}
	}

}
