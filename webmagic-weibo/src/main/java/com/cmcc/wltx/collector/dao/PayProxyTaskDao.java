package com.cmcc.wltx.collector.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.cmcc.wltx.collector.exception.DataAccessException;

public interface PayProxyTaskDao {
	String create(List<Map<String, String>> taskList) throws SQLException, DataAccessException;
}
