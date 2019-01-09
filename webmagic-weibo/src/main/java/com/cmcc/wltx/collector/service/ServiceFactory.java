package com.cmcc.wltx.collector.service;

import java.lang.reflect.Proxy;

import com.cmcc.wltx.collector.service.aspect.JDBCServiceInvocationHandler;
import com.cmcc.wltx.collector.service.impl.JDBCFollowedWeiboSpiderTaskService;
import com.cmcc.wltx.collector.service.impl.JDBCPayProxyTaskService;
import com.cmcc.wltx.collector.service.impl.JDBCSpiderService;
import com.cmcc.wltx.collector.service.impl.JDBCWeiboUserService;
import com.cmcc.wltx.collector.service.impl.JDBCWeiboVisitorCookieService;
import com.cmcc.wltx.collector.service.impl.RedisHomeWeiboSpiderTaskService;

public class ServiceFactory {
	private static SpiderService spiderService;
	private static HomeWeiboSpiderTaskService homeWeiboSpiderTaskService;
	private static WeiboUserService weiboUserService;
	private static FollowedWeiboSpiderTaskService followedWeiboSpiderTaskService;
	private static WeiboVisitorCookieService weiboVisitorCookieService;
	private static PayProxyTaskService payProxyTaskService;

	public static SpiderService getSpiderService() {
		if (null == spiderService) {
			spiderService = (SpiderService) Proxy.newProxyInstance(SpiderService.class.getClassLoader(),
					new Class[] { SpiderService.class }, new JDBCServiceInvocationHandler(new JDBCSpiderService()));
		}
		return spiderService;
	}

	public static PayProxyTaskService getPayProxyTaskService() {
		if (null == payProxyTaskService) {
			payProxyTaskService = (PayProxyTaskService) Proxy.newProxyInstance(
					PayProxyTaskService.class.getClassLoader(), new Class[] { PayProxyTaskService.class },
					new JDBCServiceInvocationHandler(new JDBCPayProxyTaskService()));
		}
		return payProxyTaskService;
	}

	public static HomeWeiboSpiderTaskService getHomeWeiboSpiderTaskService() {
		if (null == homeWeiboSpiderTaskService) {
			homeWeiboSpiderTaskService = new RedisHomeWeiboSpiderTaskService();
		}
		return homeWeiboSpiderTaskService;
	}

	public static FollowedWeiboSpiderTaskService getFollowedWeiboSpiderTaskService() {
		if (null == followedWeiboSpiderTaskService) {
			followedWeiboSpiderTaskService = (FollowedWeiboSpiderTaskService) Proxy.newProxyInstance(
					FollowedWeiboSpiderTaskService.class.getClassLoader(),
					new Class[] { FollowedWeiboSpiderTaskService.class },
					new JDBCServiceInvocationHandler(new JDBCFollowedWeiboSpiderTaskService()));
		}
		return followedWeiboSpiderTaskService;
	}

	public static WeiboVisitorCookieService getWeiboVisitorCookieService() {
		if (null == weiboVisitorCookieService) {
			weiboVisitorCookieService = (WeiboVisitorCookieService) Proxy.newProxyInstance(
					WeiboVisitorCookieService.class.getClassLoader(), new Class[] { WeiboVisitorCookieService.class },
					new JDBCServiceInvocationHandler(new JDBCWeiboVisitorCookieService()));
		}
		return weiboVisitorCookieService;
	}

	public static WeiboUserService getWeiboUserService() {
		if (null == weiboUserService) {
			weiboUserService = (WeiboUserService) Proxy.newProxyInstance(WeiboUserService.class.getClassLoader(),
					new Class[] { WeiboUserService.class },
					new JDBCServiceInvocationHandler(new JDBCWeiboUserService()));
		}
		return weiboUserService;
	}
}
