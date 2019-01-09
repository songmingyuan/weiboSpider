package us.codecraft.webmagic.exception;

import us.codecraft.webmagic.Page;

public class PageProcessException extends Exception {
	private static final long serialVersionUID = 1L;
	private Page page;

	public PageProcessException() {
		super();
	}

	public PageProcessException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PageProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	public PageProcessException(String message) {
		super(message);
	}

	public PageProcessException(Throwable cause) {
		super(cause);
	}

	public PageProcessException(String message, Page page) {
		super(message);
		this.page = page;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
	
}
