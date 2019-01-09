package com.cmcc.wltx.collector.exception;

public class LogicError extends Error {
	private static final long serialVersionUID = 1L;

	public LogicError() {
		super();
	}

	public LogicError(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LogicError(String message, Throwable cause) {
		super(message, cause);
	}

	public LogicError(String message) {
		super(message);
	}

	public LogicError(Throwable cause) {
		super(cause);
	}

}
