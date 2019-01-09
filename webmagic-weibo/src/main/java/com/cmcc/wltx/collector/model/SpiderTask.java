package com.cmcc.wltx.collector.model;

import java.util.Date;

public class SpiderTask {
	/**
	 * 正在执行的爬虫id，null表示没有爬虫在执行
	 */
	private String spiderId;
	/**
	 * 被爬虫取出时的时间
	 */
	private Date startTime;
	/**
	 * 两次被取出时间间隔下限
	 */
	private Integer minIinterval;

	public String getSpiderId() {
		return spiderId;
	}

	public void setSpiderId(String spiderId) {
		this.spiderId = spiderId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Integer getMinIinterval() {
		return minIinterval;
	}

	public void setMinIinterval(Integer minIinterval) {
		this.minIinterval = minIinterval;
	}
}
