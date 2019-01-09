package com.cmcc.wltx.collector.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cmcc.wltx.collector.dao.WeiboVisitorCookieDao;

public class JDBCWeiboVisitorCookieDao extends JDBCBasicDao implements WeiboVisitorCookieDao {
	@Override
	public List<String> getAllCookies() throws SQLException {
		try (ResultSet rs = executeQuery("select c_cookie from t_cookie_weibo_visitor")) {
			List<String> cookies = new ArrayList<String>();
			while (rs.next()) {
				cookies.add(rs.getString(1));
			}
			return cookies;
		}
	}

	@Override
	public double[] installWeiboTaskWeights(int proxy_type) throws SQLException {
		try (ResultSet rs = executeQuery("select * from t_weibo_task_properties where proxy_type = " + proxy_type)) {
			double[] weights = { 1, 1, 1, 1, 1 };
			while (rs.next()) {
				int queue_task_1_num = rs.getInt("queue_task_1_num");
				if (!rs.wasNull()) {
					weights[0] = queue_task_1_num;
				}

				int queue_task_2_num = rs.getInt("queue_task_2_num");
				if (!rs.wasNull()) {
					weights[1] = queue_task_2_num;
				}

				int queue_task_3_num = rs.getInt("queue_task_3_num");
				if (!rs.wasNull()) {
					weights[2] = queue_task_3_num;
				}

				int queue_task_4_num = rs.getInt("queue_task_4_num");
				if (!rs.wasNull()) {
					weights[3] = queue_task_4_num;
				}

				int queue_task_5_num = rs.getInt("queue_task_5_num");
				if (!rs.wasNull()) {
					weights[4] = queue_task_5_num;
				}
				break;
			}
			return weights;
		}
	}
}
