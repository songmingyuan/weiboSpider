package com.cmcc.json.analyzer;

import java.util.HashSet;
import java.util.Set;

public class JSFieldInfo {
	private String fieldName;
	private boolean valueEnumerative;
	private boolean logField = false;
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
	public String toRowString() {
		return String.format("%1$-30s%2$-10s%3$-10s%4$-10s%5$-5s%6$s",
				logField ? '#' + fieldName : fieldName, emptyCount, nullCount,
				expCount, valueEnumerative ? 1 : "", possibleValue);
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
	public boolean isLogField() {
		return logField;
	}
	public void setLogField(boolean logField) {
		this.logField = logField;
	}
}
