package com.cmcc.wltx.collector.model;


public class HomeWeiboSpiderTask {
	public static final int LEVEL_TEMP = 0;
	public static final int LEVEL_404 = 1;
	public HomeWeiboSpiderTask() {
		super();
	}

	public HomeWeiboSpiderTask(long id, int level) {
		super();
		this.id = id;
		this.level = level;
	}
	/**
	 * 微博账号ID
	 */
	private long id;
	/**
	 * 级别（大于0：普通任务；等于0：新增账号任务；小于0：基础信息补采任务）
	 */
	private int level;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
}
