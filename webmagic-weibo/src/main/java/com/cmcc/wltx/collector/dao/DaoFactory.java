package com.cmcc.wltx.collector.dao;

import java.lang.reflect.Proxy;

import com.cmcc.wltx.collector.dao.aspect.JDBCDaoInvocationHandler;
import com.cmcc.wltx.collector.dao.impl.JDBCFollowedWeiboSpiderTaskDao;
import com.cmcc.wltx.collector.dao.impl.JDBCHomeWeiboSpiderTaskDao;
import com.cmcc.wltx.collector.dao.impl.JDBCPayProxyTaskDao;
import com.cmcc.wltx.collector.dao.impl.JDBCSpiderDao;
import com.cmcc.wltx.collector.dao.impl.JDBCWeiboUserDao;
import com.cmcc.wltx.collector.dao.impl.JDBCWeiboVisitorCookieDao;

public class DaoFactory {
	private static SpiderDao spiderDao;
	private static WeiboVisitorCookieDao weiboVisitorCookieDao;
	private static FollowedWeiboSpiderTaskDao followedWeiboSpiderTaskDao;
	private static HomeWeiboSpiderTaskDao homeWeiboSpiderTaskDao;
	private static WeiboUserDao weiboUserDao;
	private static PayProxyTaskDao payProxyTaskDao;

	public static SpiderDao getSpiderDao() {
		if (null == spiderDao) {
			spiderDao = (SpiderDao) Proxy.newProxyInstance(SpiderDao.class.getClassLoader(),
					new Class[] { SpiderDao.class }, new JDBCDaoInvocationHandler(new JDBCSpiderDao()));
		}
		return spiderDao;
	}

	public static PayProxyTaskDao getPayProxyTaskDao() {
		if (null == payProxyTaskDao) {
			payProxyTaskDao = (PayProxyTaskDao) Proxy.newProxyInstance(PayProxyTaskDao.class.getClassLoader(),
					new Class[] { PayProxyTaskDao.class }, new JDBCDaoInvocationHandler(new JDBCPayProxyTaskDao()));
		}
		return payProxyTaskDao;
	}

	public static WeiboVisitorCookieDao getWeiboVisitorCookieDao() {
		if (null == weiboVisitorCookieDao) {
			weiboVisitorCookieDao = (WeiboVisitorCookieDao) Proxy.newProxyInstance(
					WeiboVisitorCookieDao.class.getClassLoader(), new Class[] { WeiboVisitorCookieDao.class },
					new JDBCDaoInvocationHandler(new JDBCWeiboVisitorCookieDao()));
		}
		return weiboVisitorCookieDao;
	}

	public static FollowedWeiboSpiderTaskDao getFollowedWeiboSpiderTaskDao() {
		if (null == followedWeiboSpiderTaskDao) {
			followedWeiboSpiderTaskDao = (FollowedWeiboSpiderTaskDao) Proxy.newProxyInstance(
					FollowedWeiboSpiderTaskDao.class.getClassLoader(), new Class[] { FollowedWeiboSpiderTaskDao.class },
					new JDBCDaoInvocationHandler(new JDBCFollowedWeiboSpiderTaskDao()));
		}
		return followedWeiboSpiderTaskDao;
	}

	public static HomeWeiboSpiderTaskDao getHomeWeiboSpiderTaskDao() {
		if (null == homeWeiboSpiderTaskDao) {
			homeWeiboSpiderTaskDao = (HomeWeiboSpiderTaskDao) Proxy.newProxyInstance(
					HomeWeiboSpiderTaskDao.class.getClassLoader(), new Class[] { HomeWeiboSpiderTaskDao.class },
					new JDBCDaoInvocationHandler(new JDBCHomeWeiboSpiderTaskDao()));
		}
		return homeWeiboSpiderTaskDao;
	}

	public static WeiboUserDao getWeiboUserDao() {
		if (null == weiboUserDao) {
			weiboUserDao = (WeiboUserDao) Proxy.newProxyInstance(WeiboUserDao.class.getClassLoader(),
					new Class[] { WeiboUserDao.class }, new JDBCDaoInvocationHandler(new JDBCWeiboUserDao()));
		}
		return weiboUserDao;
	}
}
