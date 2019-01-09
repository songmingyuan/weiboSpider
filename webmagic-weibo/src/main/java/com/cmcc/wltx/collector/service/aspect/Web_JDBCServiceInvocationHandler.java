package com.cmcc.wltx.collector.service.aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.cmcc.jdbc.MyDataSourceWeb;

public class Web_JDBCServiceInvocationHandler implements InvocationHandler {
	private Object target;

	public Web_JDBCServiceInvocationHandler(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} finally {
			MyDataSourceWeb.releaseCurrentConnection();
		}
	}

}