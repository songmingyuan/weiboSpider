package com.cmcc.wltx.collector.model;

public class FollowedWeiboSpiderTask {
	/**
	 * 微博账号ID
	 */
	private long id;
	/**
	 * 账号类型
	 */
	private int type;
	/**
	 * cookie
	 */
	private String cookie;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
