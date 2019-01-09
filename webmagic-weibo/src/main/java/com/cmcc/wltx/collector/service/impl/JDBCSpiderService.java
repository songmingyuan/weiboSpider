package com.cmcc.wltx.collector.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.dao.DaoFactory;
import com.cmcc.wltx.collector.dao.SpiderDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public class JDBCSpiderService implements SpiderService {
	private final SpiderDao spiderDao = DaoFactory.getSpiderDao();

	@Override
	public MySite buildMySite(String[] spiderIds) throws ServiceException {
		MySite site = new MySite(1);

		// IE11
		site.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");

		site.setUuid(spiderIds[spiderIds.length - 1]);
		Connection conn = MyDataSource.getCurrentConnection();
		int i = 0;
		try (PreparedStatement stmt = conn.prepareStatement("select * from t_spider where c_id=?")) {
			while (i < spiderIds.length) {
				spiderDao.loadMySite(site, stmt, spiderIds[i]);
				i++;
			}
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("爬虫[" + spiderIds[i] + "]配置加载失败", e);
		}
		return site;
	}

	@Override
	public MySite buildMySiteNew(String[] spiderIds) throws ServiceException {
		MySite site = new MySite(0);

		// IE11
		site.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");

		site.setUuid(spiderIds[spiderIds.length - 1]);
		Connection conn = MyDataSource.getCurrentConnection();
		int i = 0;
		try (PreparedStatement stmt = conn.prepareStatement("select * from t_spider where c_id=?")) {
			while (i < spiderIds.length) {
				spiderDao.loadMySite(site, stmt, spiderIds[i]);
				i++;
			}
		} catch (SQLException | DataAccessException e) {
			throw new ServiceException("爬虫[" + spiderIds[i] + "]配置加载失败", e);
		}
		return site;
	}

	@Override
	public boolean updateSpiderStatus(String spiderId, int status) throws ServiceException {
		try {
			return spiderDao.updateSpiderStatus(spiderId, status);
		} catch (SQLException e) {
			throw new ServiceException("爬虫[" + spiderId + "]状态[" + status + "]更新失败", e);
		}
	}

}
