package com.cmcc.wltx.collector.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.dao.PayProxyTaskDao;
import com.cmcc.wltx.collector.exception.DataAccessException;

public class JDBCPayProxyTaskDao extends JDBCBasicDao implements PayProxyTaskDao {

	@Override
	public String create(List<Map<String, String>> taskList) throws SQLException, DataAccessException {
		String result = null;
		if (null == taskList || taskList.size() <= 0) {
			throw new DataAccessException("proxy dataList is null");
		}
		try (PreparedStatement stmt = MyDataSource.getCurrentConnection().prepareStatement(
				"insert into t_config_pay_proxy (c_host, c_port, c_time_create, c_time_invalid) values (?,?,?,?)")) {
			for (Map<String, String> proxyMap : taskList) {
				String host = proxyMap.get("host");
				int port = Integer.valueOf(proxyMap.get("port"));
				long timeCreate = Long.valueOf(proxyMap.get("creatTime"));
				long timeInvalid = Long.valueOf(proxyMap.get("invalidTime"));
				stmt.setString(1, host);
				stmt.setInt(2, port);
				stmt.setLong(3, timeCreate);
				stmt.setLong(4, timeInvalid);
				stmt.addBatch();
			}
			stmt.executeBatch();
			result = "success";
		}
		return result;
	}
}
