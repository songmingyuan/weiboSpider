package com.cmcc.wltx.collector.constraint;

import com.cmcc.wltx.database.JedisUtils;

public class RedisClusterBasedUniqueConstraint implements UniqueConstraint {
	private final int EX;

	public RedisClusterBasedUniqueConstraint() {
		this(5184000);
	}

	public RedisClusterBasedUniqueConstraint(int ex) {
		super();
		this.EX = ex;
	}

	@Override
	public boolean check(String id) {
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("id不能为空");
		}
		long ttl = JedisUtils.jc.ttl(id);
		if (ttl < EX / 2) {
			JedisUtils.jc.setex(id, EX, "");
			if (ttl == -2) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean inspect(String key) {
		if (null == key || key.length() == 0) {
			throw new IllegalArgumentException("key不能为空");
		}
		long ttl = JedisUtils.jc.ttl(key);
		if (ttl == -2) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean checkSJ(String id) {
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("id不能为空");
		}
		long ttl = JedisUtils.jc.ttl(id);
		if (ttl == -2) {
			JedisUtils.jc.setex(id, EX, "");
			return true;
		}
		return false;
	}

}
