package com.cmcc.wltx.collector.spider.mywebmagic.downloader;
import java.io.IOException;
import java.util.Collection;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

public class MyHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {
	public MyHttpRequestRetryHandler(int retryCount,
			boolean requestSentRetryEnabled,
			Collection<Class<? extends IOException>> clazzes) {
		super(retryCount, requestSentRetryEnabled, clazzes);
	}
}