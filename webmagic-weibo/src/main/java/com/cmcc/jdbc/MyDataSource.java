package com.cmcc.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

import com.cmcc.wltx.collector.ConstantsHome;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MyDataSource {
	public static final int ERRORCODE_LOCK_WAIT_TIMEOUT = 1205;
	public static final String SQLSTATE_LOCK_WAIT_TIMEOUT = "41000";
	public static final File THROW_LOCK_WAIT_TIMEOUT = new File(
			ConstantsHome.USER_DIR + File.separatorChar + "throwSQLException"
					+ File.separatorChar + "lock" + File.separatorChar
					+ "wait_timeout");
	public static final int ERRORCODE_LOCK_DEADLOCK = 1213;
	public static final String SQLSTATE_LOCK_DEADLOCK = "40001";
	public static final File THROW_LOCK_DEADLOCK = new File(
			ConstantsHome.USER_DIR + File.separatorChar + "throwSQLException"
					+ File.separatorChar + "lock" + File.separatorChar
					+ "deadlock");

	private static final ThreadLocal<Connection> CONNECTION_CURRENT = new ThreadLocal<Connection>();
	private static final ThreadLocal<Statement> STATEMENT_CURRENT = new ThreadLocal<Statement>();
	private final static ComboPooledDataSource DS = new ComboPooledDataSource();

	/**
	 * 初始化数据源
	 */
	public static void init() {
		File propFile = new File(System.getProperty("user.dir") + File.separatorChar
							+ "dataSource.properties");
		if (!propFile.isFile()) {
			propFile = new File(propFile.getParentFile().getParentFile(), "dataSource.properties");
		}
		if (!propFile.isFile()) {
			throw new Error("数据源配置文件不存在");
		}
		try {
			Properties ppts = new Properties();
			FileInputStream in = new FileInputStream(propFile);
			ppts.load(in);
			in.close();
			DS.setDriverClass(ppts.getProperty("jdbc.driver"));
			DS.setJdbcUrl(ppts.getProperty("jdbc.url"));
			DS.setUser(ppts.getProperty("jdbc.user"));
			DS.setPassword(ppts.getProperty("jdbc.password"));
			DS.setInitialPoolSize(Integer.parseInt(ppts
					.getProperty("c3p0.initialPoolSize")));
			DS.setMinPoolSize(Integer.parseInt(ppts
					.getProperty("c3p0.minPoolSize")));
			DS.setMaxPoolSize(Integer.parseInt(ppts
					.getProperty("c3p0.maxPoolSize")));
			DS.setMaxStatements(Integer.parseInt(ppts
					.getProperty("c3p0.maxStatements")));
			DS.setMaxIdleTime(Integer.parseInt(ppts
					.getProperty("c3p0.maxIdleTime")));
		} catch (Exception e) {
			throw new Error("数据源初始化错误", e);
		}
	}

	public static Connection getCurrentConnection(){
		Connection connection = CONNECTION_CURRENT.get();
		try {
			if (null == connection || connection.isClosed()) {
				connection = connect();
				CONNECTION_CURRENT.set(connection);
			}
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		return connection;
	}

	public static void releaseCurrentConnection() {
		Statement statement = STATEMENT_CURRENT.get();
		if (null != statement) {
			STATEMENT_CURRENT.remove();
			release(statement);
		}
		
		Connection conn = CONNECTION_CURRENT.get();
		if (null != conn) {
			CONNECTION_CURRENT.remove();
			release(conn);
		}
	}

	/**
	 * 连接数据库
	 * @return
	 */
	public static Connection connect() {
		try {
			return DS.getConnection();
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
	}
	
	public static void release(ResultSet rs, Statement stmt) {
		try {
			release(rs);
		} finally {
			release(stmt);
		}
	}

	public static void release(ResultSet rs, Statement stmt, Connection conn) {
		try {
			release(rs);
		} finally {
			release(stmt, conn);
		}
	}

	public static void release(Statement stmt, Connection conn) {
		try {
			release(stmt);
		} finally {
			release(conn);
		}
	}
	
	public static void release(ResultSet rs) {
		try {
			if (null != rs && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
	}
	
	public static void release(Statement stmt) {
		try {
			if (null != stmt && !stmt.isClosed()) {
				stmt.close();
			}
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
	}
	
	/**
	 * 释放连接
	 * @param conn
	 */
	public static void release(Connection conn) {
		try {
			if (null != conn && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * 关闭数据源
	 */
	public static void destroy() {
		DS.close();
	}

	/**
	 * 产生ID
	 * @return
	 */
	public static String generateId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static Statement getCurrentStatement() {
		Statement statement = STATEMENT_CURRENT.get();
		try {
			if (null == statement || statement.isClosed()) {
				statement = getCurrentConnection().createStatement();
				STATEMENT_CURRENT.set(statement);
			}
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		return statement;
	}
}
