package com.cmcc.wltx.collector.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.dao.BasicDao;

public class JDBCBasicDao implements BasicDao {
	@Override
	public int executeUpdate(String sql) throws SQLException {
		return MyDataSource.getCurrentStatement().executeUpdate(sql);
	}
	
	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		return MyDataSource.getCurrentStatement().executeQuery(sql);
	}
}
