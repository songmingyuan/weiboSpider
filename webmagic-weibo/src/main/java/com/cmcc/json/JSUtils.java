package com.cmcc.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cmcc.json.impl.JSArrayImpl;
import com.cmcc.json.impl.JSObjectImpl;

public class JSUtils {
	public static JSObject createJSObject(String source) throws JSException {
		try {
			return new JSObjectImpl(new JSONObject(source));
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	public static JSArray createJSArray(String source) throws JSException {
		try {
			return new JSArrayImpl(new JSONArray(source));
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}
}
