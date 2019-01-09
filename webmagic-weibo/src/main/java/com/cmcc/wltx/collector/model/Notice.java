package com.cmcc.wltx.collector.model;

import java.util.Date;

public class Notice {
	public static final int TYPE_UPDATETEMPLATE = 1;
	public static final int TYPE_UPDATEPROPERTIES = 2;
	public static final int TYPE_UPDATEBUSINESSTYPE = 3;
	private String id;
	private String spiderId;
	/**
	 * 通知类型
	 * 1:模板更新; 2:规则更新; 3:业务类型更新;
	 */
	private int type;
	private String content;
	private Date effectedTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSpiderId() {
		return spiderId;
	}

	public void setSpiderId(String spiderId) {
		this.spiderId = spiderId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getEffectedTime() {
		return effectedTime;
	}

	public void setEffectedTime(Date effectedTime) {
		this.effectedTime = effectedTime;
	}
}
