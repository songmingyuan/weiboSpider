package com.cmcc.json;

public interface JSArray {
	int length();

	JSObject getNotNullJSObject(int index) throws JSException;

	String getNotNullString(int index) throws JSException;
	
	String toFormattedString();

	JSArray getNotNullJSArray(int i) throws JSException;
}
