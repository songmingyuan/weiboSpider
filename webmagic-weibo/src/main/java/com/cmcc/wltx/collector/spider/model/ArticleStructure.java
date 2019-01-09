package com.cmcc.wltx.collector.spider.model;

import java.util.ArrayList;
import java.util.List;

public class ArticleStructure {
	private int count;
	private List<FieldInfo> fieldInfos;
	
	public ArticleStructure(String fieldNamesStr) {
		super();
		String[] fieldNames = fieldNamesStr.split(", ");
		fieldInfos = new ArrayList<FieldInfo>(fieldNames.length);
		for (String fieldName : fieldNames) {
			FieldInfo fieldInfo = new FieldInfo();
			if (fieldName.startsWith("#")) {
				fieldName = fieldName.substring(1);
				fieldInfo.setValueEnumerative(true);
			}
			fieldInfo.setFieldName(fieldName);
			fieldInfos.add(fieldInfo);
		}
	}
	
	public void articlePlus(){
		count++;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<FieldInfo> getFieldInfos() {
		return fieldInfos;
	}
	public void setFieldInfos(List<FieldInfo> fieldInfos) {
		this.fieldInfos = fieldInfos;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\n");
		sb.append(String.format("%1$-30s%2$-10s%3$-10s%4$-10s%5$-5s%6$s", ("articleCount = " + count) , "empty", "null", "exception", ">20", "possibleValue")).append('\n');
//		sb.append("articleCount = ").append(count).append('\n');
		for (FieldInfo fieldInfo : fieldInfos) {
			sb.append(fieldInfo.toRowString()).append('\n');
		}
		return sb.toString();
	}
	
}
