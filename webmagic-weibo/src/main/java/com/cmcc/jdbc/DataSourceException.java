package com.cmcc.jdbc;

public class DataSourceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DataSourceException() {
		super();
	}

	public DataSourceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataSourceException(String message) {
		super(message);
	}

	public DataSourceException(Throwable cause) {
		super(cause);
	}

}
