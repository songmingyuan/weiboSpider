package com.cmcc.json;

import java.util.Set;

public interface JSObject {
	Set<String> keySet();

	/**
	 * 可能返回null
	 * @param key
	 * @return
	 * @throws JSException
	 */
	String getString(String key) throws JSException;

	/**
	 * null时抛出异常
	 * @param key
	 * @return
	 * @throws JSException
	 */
	String getNotNullString(String key) throws JSException;

	int getNotNullInt(String key) throws JSException;

	JSArray getNotNullJSArray(String string) throws JSException;

	Object get(String key) throws JSException;

	JSObject getJSObject(String key) throws JSException;
	JSObject getNotNullJSObject(String key) throws JSException;
	
	String toFormattedString();

	long getNotNullLong(String key) throws JSException;

	boolean getNotNullBoolean(String key) throws JSException;
}
