package com.cmcc.wltx.collector.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cmcc.wltx.collector.dao.FollowedWeiboSpiderTaskDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.model.FollowedWeiboSpiderTask;

public class JDBCFollowedWeiboSpiderTaskDao extends JDBCBasicDao implements
		FollowedWeiboSpiderTaskDao {

	@Override
	public void releaseTasks(String spiderId) throws SQLException,
			DataAccessException {
		if (null == spiderId) {
			throw new DataAccessException("null == spiderId");
		}
		executeUpdate("update t_task_spider_weibo_followed set c_id_spider=NULL where c_id_spider='"
				+ spiderId + '\'');
	}

	@Override
	public int updateForCrawl(String spiderId, int limit) throws SQLException,
			DataAccessException {
		if (null == spiderId) {
			throw new DataAccessException("null == spiderId");
		}
		if (limit <= 0) {
			throw new DataAccessException("limit <= 0");
		}
		long now = System.currentTimeMillis();
		return executeUpdate("update t_task_spider_weibo_followed set c_id_spider='"
				+ spiderId
				+ "',c_time_start="
				+ now
				+ " where c_id_spider is null and c_cookie!='' and "
				+ now
				+ "-c_time_start>c_interval_min order by c_interval_min, c_time_start limit "
				+ limit);
	}

	@Override
	public List<FollowedWeiboSpiderTask> listBySpiderId(String spiderId)
			throws SQLException, DataAccessException {
		if (null == spiderId) {
			throw new DataAccessException("null == spiderId");
		}
		try (ResultSet rs = executeQuery("select * from t_task_spider_weibo_followed where c_id_spider='"
				+ spiderId + '\'')) {
			return loadTaskFromResultSet(rs);
		}
	}
	
	private List<FollowedWeiboSpiderTask> loadTaskFromResultSet(ResultSet rs) throws SQLException{
		List<FollowedWeiboSpiderTask> res = new ArrayList<FollowedWeiboSpiderTask>();
		while (rs.next()) {
			FollowedWeiboSpiderTask task = new FollowedWeiboSpiderTask();
			long id = rs.getLong("c_id");
			if (!rs.wasNull()) {
				task.setId(id);
			}
			String cookie = rs.getString("c_cookie");
			if (null != cookie && (cookie = cookie.trim()).length() != 0) {
				task.setCookie(cookie);
			}
			int type = rs.getInt("c_type");
			if (!rs.wasNull()) {
				task.setType(type);
			}
			res.add(task);
		}
		return res;
	}

	@Override
	public List<FollowedWeiboSpiderTask> specialListBySpiderId(String spiderId)
			throws SQLException, DataAccessException {
		if (null == spiderId) {
			throw new DataAccessException("null == spiderId");
		}
		try (ResultSet rs = executeQuery("select * from t_task_spider_weibo_followed where c_id_spider='"
				+ spiderId + "' and c_cookie!=''")) {
			return loadTaskFromResultSet(rs);
		}
	}

	@Override
	public void updateCookieById(long id, String cookie) throws SQLException {
		executeUpdate("update t_task_spider_weibo_followed set c_cookie='"
				+ cookie + "' where c_id=" + id);
	}

}
