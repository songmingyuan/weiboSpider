package com.cmcc.wltx.collector.dao;

import java.sql.SQLException;

import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.model.HomeWeiboSpiderTask;

public interface HomeWeiboSpiderTaskDao extends BasicDao {
	void create(HomeWeiboSpiderTask task) throws SQLException, DataAccessException;
	int delete(HomeWeiboSpiderTask task) throws SQLException;
}
