package com.cmcc.json.impl;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.cmcc.json.JSArray;
import com.cmcc.json.JSException;
import com.cmcc.json.JSObject;

public class JSObjectImpl implements JSObject {
	private JSONObject target;

	public JSObjectImpl(JSONObject target) {
		super();
		this.target = target;
	}

	@Override
	public int getNotNullInt(String key) throws JSException {
		try {
			return target.getInt(key);
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> keySet() {
		return target.keySet();
	}

	@Override
	public String getString(String key) throws JSException {
		try {
	        Object object = target.get(key);
	        if (object instanceof String) {
	            return (String) object;
	        }
	        if (object.equals(null)) {
				return null;
			}
	        throw new JSONException("JSONObject[" + JSONObject.quote(key) + "] not a string.");
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}
	
	@Override
	public String getNotNullString(String key) throws JSException {
		try {
			return target.getString(key);
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@Override
	public String toString() {
		return target.toString();
	}

	@Override
	public JSArray getNotNullJSArray(String key) throws JSException {
		try {
			return new JSArrayImpl(target.getJSONArray(key));
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@Override
	public Object get(String key) throws JSException {
		try {
			Object obj = target.get(key);
			if (obj.equals(null)) {
				return null;
			}
			return obj;
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}
	
	@Override
	public JSObject getJSObject(String key) throws JSException {
		Object object;
		try {
			object = target.get(key);
		} catch (JSONException e) {
			throw new JSException(e);
		}

		if (object instanceof JSONObject) {
			return new JSObjectImpl(target.getJSONObject(key));
		}
		if (JSONObject.NULL == object) {
			return null;
		}
		throw new JSException("JSONObject[" + key + "] is not a JSONObject.");
	}

	@Override
	public JSObject getNotNullJSObject(String key) throws JSException {
		try {
			return new JSObjectImpl(target.getJSONObject(key));
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@Override
	public String toFormattedString() {
		return target.toString(4);
	}

	@Override
	public long getNotNullLong(String key) throws JSException {
		try {
			return target.getLong(key);
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

	@Override
	public boolean getNotNullBoolean(String key) throws JSException {
		try {
			return target.getBoolean(key);
		} catch (JSONException e) {
			throw new JSException(e);
		}
	}

}
