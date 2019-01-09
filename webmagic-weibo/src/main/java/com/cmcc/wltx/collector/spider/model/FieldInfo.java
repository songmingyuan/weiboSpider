package com.cmcc.wltx.collector.spider.model;

import java.util.HashSet;
import java.util.Set;

public class FieldInfo {
	private String fieldName;
	private boolean valueEnumerative;
	private Set<String> possibleValue = new HashSet<String>();
	private int emptyCount;
	private int expCount;
	private int nullCount;
	
	public void emptyPlus(){
		emptyCount++;
	}
	public void nullPlus(){
		nullCount++;
	}
	public void expPlus(){
		expCount++;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Set<String> getPossibleValue() {
		return possibleValue;
	}
	public void setPossibleValue(Set<String> possibleValue) {
		this.possibleValue = possibleValue;
	}
	public int getEmptyCount() {
		return emptyCount;
	}
	public void setEmptyCount(int emptyCount) {
		this.emptyCount = emptyCount;
	}
	public int getExpCount() {
		return expCount;
	}
	public void setExpCount(int expCount) {
		this.expCount = expCount;
	}
	@Override
	public String toString() {
		return String.format("%1$-30semptyCount=%2$-20snullCount=%3$-20sexpCount=%4$-20spossibleValue=%5$s", (fieldName + ':') , emptyCount, nullCount, expCount, possibleValue);
	}
	public String toRowString() {
		return String.format("%1$-30s%2$-10s%3$-10s%4$-10s%5$-5s%6$s", (fieldName) , emptyCount, nullCount, expCount, valueEnumerative?1:"", possibleValue);
	}
	public boolean isValueEnumerative() {
		return valueEnumerative;
	}
	public void setValueEnumerative(boolean valueEnumerative) {
		this.valueEnumerative = valueEnumerative;
	}
	public int getNullCount() {
		return nullCount;
	}
	public void setNullCount(int nullCount) {
		this.nullCount = nullCount;
	}
}
