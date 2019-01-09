package com.cmcc.json;

public class JSException extends RuntimeException {
	private static final long serialVersionUID = -1231923678017426600L;

	public JSException() {
		super();
	}

	public JSException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JSException(String message, Throwable cause) {
		super(message, cause);
	}

	public JSException(String message) {
		super(message);
	}

	public JSException(Throwable cause) {
		super(cause);
	}

}
