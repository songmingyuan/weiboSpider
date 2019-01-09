package com.cmcc.wltx.collector.service.aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.cmcc.jdbc.MyDataSource;

public class JDBCServiceInvocationHandler implements InvocationHandler {
	private Object target;
	
	public JDBCServiceInvocationHandler(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} finally {
			MyDataSource.releaseCurrentConnection();
		}
	}

}
