package us.codecraft.webmagic;

import java.io.Serializable;

import org.apache.http.HttpStatus;

public class Response implements Serializable {
	private static final long serialVersionUID = -8387765785396134750L;
	private int statusCode = HttpStatus.SC_OK;
	private Throwable e;
	private String contentType;
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	private byte[] content;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public Throwable getE() {
		return e;
	}

	public void setE(Throwable e) {
		this.e = e;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

}
