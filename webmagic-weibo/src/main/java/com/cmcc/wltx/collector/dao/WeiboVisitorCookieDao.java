package com.cmcc.wltx.collector.dao;

import java.sql.SQLException;
import java.util.List;

public interface WeiboVisitorCookieDao extends BasicDao {
	List<String> getAllCookies() throws SQLException;

	double[] installWeiboTaskWeights(int proxy_type) throws SQLException;
}
