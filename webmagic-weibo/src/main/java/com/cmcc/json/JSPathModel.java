package com.cmcc.json;

public interface JSPathModel {
	<T> T read(String path) throws JSException;

	JSObject readJSObject(String path) throws JSException;

	JSArray readJSArray(String path) throws JSException;
}
