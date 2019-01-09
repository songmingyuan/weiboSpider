package com.cmcc.wltx.collector.spider.model;

import java.util.List;

public class ArticleItem {
	private String name;
	private boolean required;
	private List<String> xpaths;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getXpaths() {
		return xpaths;
	}

	public void setXpaths(List<String> xpaths) {
		this.xpaths = xpaths;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
