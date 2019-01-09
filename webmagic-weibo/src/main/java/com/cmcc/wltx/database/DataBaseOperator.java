package com.cmcc.wltx.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作类
 * 
 * @author liping
 * 
 */
public class DataBaseOperator {

	private Connection connection;

	public DataBaseOperator() {
		// init();
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * 根据sql查询返回对象集合
	 * 
	 * @param clz
	 *            返回对象的类型
	 * @param sql
	 *            sql语句
	 * @param params
	 *            查询参数
	 * @return
	 */
	public <T> List<T> query(Class<T> clz, String sql, Object[] params) {
		Method[] allMethods = clz.getDeclaredMethods();
		Map<String, Method> methods = new HashMap<String, Method>();
		for (Method method : allMethods) {
			if (method.getName().startsWith("set")) {
				String fieldName = method.getName().substring(3).toLowerCase();
				methods.put(fieldName, method);
			}
		}
		ResultSet resultset = query(sql, params);
		List<T> result = new ArrayList<T>();
		try {
			ResultSetMetaData metaData = resultset.getMetaData();
			int columnCount = metaData.getColumnCount();
			while (resultset.next()) {
				T obj = clz.newInstance();
				for (int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnName(i).toLowerCase();
					Method method = methods.get(columnName);
					Object value = resultset.getObject(columnName);
					if (value == null)
						continue;
					method.invoke(obj, value);
				}
				result.add(obj);
			}
			resultset.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据sql语句和参数查询返回 java.sql.ResultSet
	 * 
	 * @param sql
	 *            查询语句
	 * @param parameters
	 *            查询参数
	 * @return
	 */
	public ResultSet query(String sql, Object[] parameters) {
		try {
			init();
			PreparedStatement prestmt = connection.prepareStatement(sql);
			if (parameters != null && parameters.length > 0) {
				for (int i = 0; i < parameters.length; i++) {
					prestmt.setObject(i + 1, parameters[i]);
				}
			}
			return prestmt.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}

	public int execute(String sql, Object[] params) {
		int result = 0;
		try {
			init();
			connection.setAutoCommit(false);
			PreparedStatement prestmt = connection.prepareStatement(sql);
			if (params != null && params.length > 0) {
				for (int i = 0; i < params.length; i++) {
					prestmt.setObject(i + 1, params[i]);
				}
			}
			result = prestmt.executeUpdate();
			if (result < 0) {
				connection.rollback();
			} else {
				connection.commit();
			}
			prestmt.close();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException se) {
				e.printStackTrace();
			}
			return -1;
		}
		return result;
	}

	// 使用PreparedStatement 批处理
	public void execBatch(String sql, List<Object[]> params) {
		try {
			init();
			connection.setAutoCommit(false);
			PreparedStatement prestmt = connection.prepareStatement(sql);
			Object[] details = null;
			int paSize = params.size();
			for (int i = 1; i <= paSize; i++) {
				details = params.get(i - 1);
				for (int num = 0; num < details.length; num++) {
					prestmt.setObject(num + 1, details[num]);
				}
				prestmt.addBatch();
				if (i % 1000 == 0) {
					prestmt.executeBatch();
					connection.commit();
					prestmt.clearBatch();
				}
			}
			if (paSize % 1000 > 0) {// 做一个补充
				prestmt.executeBatch();
				connection.commit();
				prestmt.clearBatch();
			}
			prestmt.close();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException se) {
				e.printStackTrace();
			}
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭数据库连接
	 */
	public void close() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化数据库连接
	 */
	public void init() {
		try {
			if (connection == null || connection.isClosed()) {
				connection = DbPool.getInstance().getConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException {
		DataBaseOperator db = new DataBaseOperator();
		String sql = "select * from gx_log LIMIT 3";
		ResultSet resultset = db.query(sql, null);
		while (resultset.next()) {
			resultset.getObject(1);
		}
	}
}
