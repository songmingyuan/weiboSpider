package com.cmcc.wltx.collector.service.impl;

import java.sql.SQLException;

import com.cmcc.wltx.collector.dao.DaoFactory;
import com.cmcc.wltx.collector.dao.WeiboUserDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.WeiboUser;
import com.cmcc.wltx.collector.service.WeiboUserService;

public class JDBCWeiboUserService implements
		WeiboUserService {
	private final WeiboUserDao weiboUserDao = DaoFactory
			.getWeiboUserDao();

	@Override
	public void update(WeiboUser user, String tableSuffix) throws ServiceException {
		try {
			weiboUserDao.update(user, tableSuffix);
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("微博用户[" + user.getId() + "]更新异常", e);
		}
	}

	@Override
	public void updateStatusById(int status, long id)
			throws ServiceException {
		try {
			weiboUserDao.updateStatusById(status, id);
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("微博用户[" + id + "]状态更新异常", e);
		}
	}
}
