package com.cmcc.wltx.collector.constraint;

import com.cmcc.wltx.database.JedisUtils;

import redis.clients.jedis.Jedis;

public class RedisBasedUniqueConstraint implements UniqueConstraint {
	/**
	 * 过期时间
	 */
	private final int EX;

	public RedisBasedUniqueConstraint() {
		// 默认60天过期
		this(5184000);
	}

	public RedisBasedUniqueConstraint(int ex) {
		super();
		this.EX = ex;
	}

	@Override
	public boolean check(String id) {
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("id不能为空");
		}
		try (Jedis jedis = JedisUtils.createJedis()) {
			long ttl = jedis.ttl(id);
			if (ttl < EX / 2) {
				jedis.setex(id, EX, "");
				if (ttl == -2) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean inspect(String key) {
		if (null == key || key.length() == 0) {
			throw new IllegalArgumentException("key不能为空");
		}
		try (Jedis jedis = JedisUtils.createJedis()) {
			long ttl = jedis.ttl(key);
			if (ttl == -2) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean checkSJ(String id) {
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("id不能为空");
		}
		try (Jedis jedis = JedisUtils.createJedis()) {
			long ttl = jedis.ttl(id);
			if (ttl == -2) {
				jedis.setex(id, EX, "");
				return true;
			}
			return false;
		}
	}

}
