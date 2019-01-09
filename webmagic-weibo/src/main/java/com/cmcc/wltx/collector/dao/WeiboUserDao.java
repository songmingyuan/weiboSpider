package com.cmcc.wltx.collector.dao;

import java.sql.SQLException;

import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.model.WeiboUser;

public interface WeiboUserDao extends BasicDao {
	void update(WeiboUser user, String tableSuffix) throws SQLException, DataAccessException;

	void updateStatusById(int status, long id) throws SQLException, DataAccessException;
}
