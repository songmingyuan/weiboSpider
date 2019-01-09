package com.cmcc.wltx.collector.service.impl;

import java.sql.SQLException;
import java.util.List;

import com.cmcc.wltx.collector.dao.DaoFactory;
import com.cmcc.wltx.collector.dao.WeiboVisitorCookieDao;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.service.WeiboVisitorCookieService;

public class JDBCWeiboVisitorCookieService implements WeiboVisitorCookieService {
	private WeiboVisitorCookieDao weiboVisitorCookieDao = DaoFactory.getWeiboVisitorCookieDao();

	@Override
	public List<String> getAllCookies() throws ServiceException {
		try {
			return weiboVisitorCookieDao.getAllCookies();
		} catch (SQLException e) {
			throw new ServiceException("微博访客cookie获取失败", e);
		}
	}

	@Override
	public double[] installWeiboTaskWeights(int proxy_type) throws ServiceException {
		try {
			return weiboVisitorCookieDao.installWeiboTaskWeights(proxy_type);
		} catch (SQLException e) {
			throw new ServiceException("微博任务队列取值频率获取失败", e);
		}
	}
}
