package com.cmcc.json.impl;

import org.json.JSONArray;
import org.json.JSONException;

import com.cmcc.json.JSArray;
import com.cmcc.json.JSException;
import com.cmcc.json.JSObject;

public class JSArrayImpl implements JSArray {
	private JSONArray target;

	public JSArrayImpl(JSONArray target) {
		super();
		this.target = target;
	}

	@Override
	public int length() {
		return target.length();
	}

	@Override
	public JSObject getNotNullJSObject(int index) throws JSException {
		try {
			return new JSObjectImpl(target.getJSONObject(index));
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@Override
	public JSArray getNotNullJSArray(int index) throws JSException {
		try {
			return new JSArrayImpl(target.getJSONArray(index));
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@Override
	public String getNotNullString(int index) throws JSException {
		try {
			return target.getString(index);
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}
	
	@Override
	public String toString() {
		return target.toString();
	}

	@Override
	public String toFormattedString() {
		return target.toString(4);
	}

}
