package com.cmcc.wltx.collector.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmcc.jdbc.MyDataSourceWeb;
import com.cmcc.wltx.collector.dao.BasicDao;

public class JDBCWebBasicDao implements BasicDao {
	@Override
	public int executeUpdate(String sql) throws SQLException {
		return MyDataSourceWeb.getCurrentStatement().executeUpdate(sql);
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		return MyDataSourceWeb.getCurrentStatement().executeQuery(sql);
	}
}
