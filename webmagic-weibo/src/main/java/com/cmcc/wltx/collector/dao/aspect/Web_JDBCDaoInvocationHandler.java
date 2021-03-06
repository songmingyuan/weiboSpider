package com.cmcc.wltx.collector.dao.aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import com.cmcc.jdbc.MyDataSourceWeb;

public class Web_JDBCDaoInvocationHandler implements InvocationHandler {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(Web_JDBCDaoInvocationHandler.class);
	private Object target;

	public Web_JDBCDaoInvocationHandler(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		do {
			try {
				return method.invoke(target, args);
			} catch (InvocationTargetException e) {
				Throwable te = e.getTargetException();
				if (te instanceof SQLException) {
					if (!needReInvoke((SQLException) te)) {
						throw te;
					}
				} else {
					throw te;
				}
			}
		} while (true);
	}

	private boolean needReInvoke(SQLException se) {
		int errorCode = se.getErrorCode();
		String sqlState = se.getSQLState();
		logger.warn("caught SQLException[{},{}]", errorCode, sqlState);

		if (MyDataSourceWeb.ERRORCODE_LOCK_WAIT_TIMEOUT == errorCode
				&& MyDataSourceWeb.SQLSTATE_LOCK_WAIT_TIMEOUT.equals(sqlState)
				&& !MyDataSourceWeb.THROW_LOCK_WAIT_TIMEOUT.exists()) {
			return true;
		}

		if (MyDataSourceWeb.ERRORCODE_LOCK_DEADLOCK == errorCode
				&& MyDataSourceWeb.SQLSTATE_LOCK_DEADLOCK.equals(sqlState)
				&& !MyDataSourceWeb.THROW_LOCK_DEADLOCK.exists()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			return true;
		}

		return false;
	}

}
