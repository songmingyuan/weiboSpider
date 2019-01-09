package com.cmcc.wltx.collector.service.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.cmcc.wltx.collector.dao.DaoFactory;
import com.cmcc.wltx.collector.dao.PayProxyTaskDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.service.PayProxyTaskService;

public class JDBCPayProxyTaskService implements PayProxyTaskService {
	private final PayProxyTaskDao payProxyTaskDao = DaoFactory.getPayProxyTaskDao();

	@Override
	public String createProxyDataList(List<Map<String, String>> taskList) throws ServiceException {
		String result = "success";
		if (null != taskList && taskList.size() > 0) {
			try {
				return payProxyTaskDao.create(taskList);
			} catch (SQLException | DataAccessException e) {
				throw new ServiceException("付费代理创建失败 ", e);
			}
		}
		return result;
	}

}
