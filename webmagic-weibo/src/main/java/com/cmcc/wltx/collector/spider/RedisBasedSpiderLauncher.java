package com.cmcc.wltx.collector.spider;

import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;
import com.cmcc.wltx.database.JedisUtils;

public class RedisBasedSpiderLauncher implements SpiderLauncher {
	private final SpiderLauncher target;

	public RedisBasedSpiderLauncher(SpiderLauncher target) {
		super();
		this.target = target;
	}

	public void launchSpider(MySite site) {
		JedisUtils.initPool(site.getRedisHost(), site.getRedisPort());
		try {
			target.launchSpider(site);
		} finally {
			JedisUtils.closePool();
		}
	}
}
