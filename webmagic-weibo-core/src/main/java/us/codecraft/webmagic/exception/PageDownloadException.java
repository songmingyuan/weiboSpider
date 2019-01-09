package us.codecraft.webmagic.exception;

public class PageDownloadException extends Exception {
	private static final long serialVersionUID = 1L;
	private int statusCode;

	public PageDownloadException() {
		super();
	}

	public PageDownloadException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PageDownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	public PageDownloadException(String message) {
		super(message);
	}

	public PageDownloadException(Throwable cause) {
		super(cause);
	}

	public PageDownloadException(String message, int statusCode) {
		super(message);
		this.setStatusCode(statusCode);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
