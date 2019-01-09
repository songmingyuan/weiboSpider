package com.cmcc.wltx.collector.service;

import java.util.List;

import com.cmcc.wltx.collector.exception.ServiceException;

public interface WeiboVisitorCookieService {
	List<String> getAllCookies() throws ServiceException;

	/**
	 * 根据代理不同，调整任务获取基数
	 * @param proxy_type
	 * @return
	 * @throws ServiceException
	 */
	double[] installWeiboTaskWeights(int proxy_type) throws ServiceException;
}
