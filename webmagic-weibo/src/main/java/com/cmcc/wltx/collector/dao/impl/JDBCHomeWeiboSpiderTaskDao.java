package com.cmcc.wltx.collector.dao.impl;

import java.sql.SQLException;

import com.cmcc.wltx.collector.dao.HomeWeiboSpiderTaskDao;
import com.cmcc.wltx.collector.model.HomeWeiboSpiderTask;

public class JDBCHomeWeiboSpiderTaskDao extends JDBCBasicDao implements
		HomeWeiboSpiderTaskDao {

	@Override
	public void create(HomeWeiboSpiderTask task) throws SQLException {
		executeUpdate("insert into t_task_spider_weibo_home_" + task.getLevel()
				+ "(c_id) values(" + task.getId() + ')');
	}

	@Override
	public int delete(HomeWeiboSpiderTask task) throws SQLException {
		return executeUpdate("delete from t_task_spider_weibo_home_"
				+ task.getLevel() + " where c_id=" + task.getId());
	}
}
