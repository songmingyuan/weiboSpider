package com.cmcc.wltx.collector.service;

import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.WeiboUser;

public interface WeiboUserService {
	void update(WeiboUser user, String tableSuffix) throws ServiceException;

	void updateStatusById(int status, long id) throws ServiceException;
}
