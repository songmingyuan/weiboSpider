package com.cmcc.wltx.database;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.cmcc.wltx.utils.PropertiesLoader;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DbPool {
	
	private String driver;
	private String url;
	private String userName;
	private String passWord;
	private String initialPoolSize;
	private String minPoolSize;
	private String maxPoolSize;
	private String maxStatements;
	private String maxIdleTime;
	
	private static DbPool DbPool;
	private ComboPooledDataSource dataSource;

	static {
		DbPool = new DbPool();
	}

	public DbPool(){       
	   try{       
		   this.driver = PropertiesLoader.getString("jdbc.driver");
		   this.url = PropertiesLoader.getString("jdbc.url");
		   this.userName = PropertiesLoader.getString("jdbc.user");
		   this.passWord = PropertiesLoader.getString("jdbc.password");
		   this.initialPoolSize = PropertiesLoader.getString("jdbc.initialPoolSize");
		   this.minPoolSize = PropertiesLoader.getString("jdbc.minPoolSize");
		   this.maxPoolSize = PropertiesLoader.getString("jdbc.maxPoolSize");
		   this.maxStatements = PropertiesLoader.getString("jdbc.maxStatements");
		   this.maxIdleTime = PropertiesLoader.getString("jdbc.maxIdleTime");
		   
	       dataSource = new ComboPooledDataSource();       
	       dataSource.setUser(userName);       
		   dataSource.setPassword(passWord);       
		   dataSource.setJdbcUrl(url); 
		   dataSource.setDriverClass(driver); 
	       dataSource.setInitialPoolSize(new Integer(initialPoolSize));//初始化时创建的连接数，应在minPoolSize与maxPoolSize之间取值 
	       dataSource.setMinPoolSize(new Integer(minPoolSize));  //连接池中保留的最小连接数
	       dataSource.setMaxPoolSize(new Integer(maxPoolSize)); //连接池中保留的最大连接数 
	       
	       //JDBC 的标准参数，用以控制数据源内加载的PreparedStatement数量。
	       //但由于预缓存的Statement属 于单个Connection而不是整 个连接池。
	       //所以设置这个参数需要考虑到多方面的因素，如果maxStatements与 maxStatementsPerConnection均为0， 则缓存被关闭。默认为0； 
	       dataSource.setMaxStatements(new Integer(maxStatements)); 
	       dataSource.setMaxIdleTime(new Integer(maxIdleTime)); //最大空闲时间，超过空闲时间的连接将被丢弃。为0或负数则永不丢弃。默认为0；
	   } catch (PropertyVetoException e) {       
	       throw new RuntimeException(e);       
	   }       
	}	
	
	public final static DbPool getInstance() {
		return DbPool;
	}

	public final Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("无法从数据源获取连接 ", e);
		}
	}

	public static void main(String[] args) throws SQLException {
		Connection con = null;
		try {
			con = DbPool.getInstance().getConnection();
		} catch (Exception e) {
		} finally {
			if (con != null)
				con.close();
		}
	}
}
