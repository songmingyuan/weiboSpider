package com.cmcc.wltx.collector.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public interface SpiderDao extends BasicDao {
	boolean updateSpiderStatus(String spiderId, int status) throws SQLException;

	void loadMySite(MySite site, PreparedStatement stmt, String string) throws SQLException, DataAccessException;
}
