package com.cmcc.wltx.collector.service;

import java.util.List;
import java.util.Map;

import com.cmcc.wltx.collector.exception.ServiceException;

public interface PayProxyTaskService {
	/**
	 * 新建任务
	 * 
	 * @param taskList
	 * @throws ServiceException
	 */
	String createProxyDataList(List<Map<String, String>> taskList) throws ServiceException;
}
