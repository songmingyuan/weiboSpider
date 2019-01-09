package com.cmcc.wltx.collector.spider.mywebmagic.downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

import us.codecraft.webmagic.Site;

public class MyHttpClientGenerator {

	private PoolingHttpClientConnectionManager connectionManager;

	public MyHttpClientGenerator() {
		Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
		connectionManager = new PoolingHttpClientConnectionManager(reg);
		connectionManager.setDefaultMaxPerRoute(100);
	}

	public MyHttpClientGenerator setPoolSize(int poolSize) {
		connectionManager.setMaxTotal(poolSize);
		return this;
	}

	public CloseableHttpClient getClient(Site site) {
		return generateClient(site);
	}

	public CloseableHttpClient getClientNew(Site site, HttpHost httpProxy) {
		String proxyUser = "yuncaispider"; // 平台帐号
		String proxyPasswd = "zyzxyuncai"; // 密码
		return generateClientNew(site, httpProxy, proxyUser, proxyPasswd);
	}

	private CloseableHttpClient generateClientNew(Site site, HttpHost realHttpProxy, String proxyUser,
			String proxyPasswd) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
		if (site != null && site.getUserAgent() != null) {
			httpClientBuilder.setUserAgent(site.getUserAgent());
		} else {
			httpClientBuilder.setUserAgent("");
		}
		if (site == null || site.isUseGzip()) {
			httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {

				public void process(final HttpRequest request, final HttpContext context)
						throws HttpException, IOException {
					if (!request.containsHeader("Accept-Encoding")) {
						request.addHeader("Accept-Encoding", "gzip");
					}

				}
			});
		}
		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(site.getTimeOut()).setSoKeepAlive(true)
				.setTcpNoDelay(true).build();
		connectionManager.setDefaultSocketConfig(socketConfig);
		if (site != null) {
			httpClientBuilder.setRetryHandler(new MyHttpRequestRetryHandler(site.getRetryTimes(), true,
					new ArrayList<Class<? extends IOException>>()));
			HttpHost httpProxy = site.getHttpProxy();
			if (null != httpProxy) {
				DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpProxy);
				httpClientBuilder.setRoutePlanner(routePlanner);
			}
		}
		// 验证付费代理以及用户验证
		if (realHttpProxy != null && StringUtils.isNotBlank(proxyUser) && StringUtils.isNotBlank(proxyPasswd)) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(realHttpProxy),
					new UsernamePasswordCredentials(proxyUser, proxyPasswd));
			httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
		}
		generateCookie(httpClientBuilder, site);
		return httpClientBuilder.build();
	}

	private CloseableHttpClient generateClient(Site site) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
		if (site != null && site.getUserAgent() != null) {
			httpClientBuilder.setUserAgent(site.getUserAgent());
		} else {
			httpClientBuilder.setUserAgent("");
		}
		if (site == null || site.isUseGzip()) {
			httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {

				public void process(final HttpRequest request, final HttpContext context)
						throws HttpException, IOException {
					if (!request.containsHeader("Accept-Encoding")) {
						request.addHeader("Accept-Encoding", "gzip");
					}

				}
			});
		}
		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(site.getTimeOut()).setSoKeepAlive(true)
				.setTcpNoDelay(true).build();
		connectionManager.setDefaultSocketConfig(socketConfig);
		if (site != null) {
			httpClientBuilder.setRetryHandler(new MyHttpRequestRetryHandler(site.getRetryTimes(), true,
					new ArrayList<Class<? extends IOException>>()));
			HttpHost httpProxy = site.getHttpProxy();
			if (null != httpProxy) {
				DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpProxy);
				httpClientBuilder.setRoutePlanner(routePlanner);
			}
		}
		generateCookie(httpClientBuilder, site);
		return httpClientBuilder.build();
	}

	private void generateCookie(HttpClientBuilder httpClientBuilder, Site site) {
		CookieStore cookieStore = null;
		if (site instanceof MySite && !((MySite) site).isStoreCookie()) {
			cookieStore = new CookieStore() {
				@Override
				public List<Cookie> getCookies() {
					return Collections.emptyList();
				}

				@Override
				public boolean clearExpired(Date date) {
					return true;
				}

				@Override
				public void clear() {
				}

				@Override
				public void addCookie(Cookie cookie) {
				}
			};
		}
		if (null == cookieStore) {
			cookieStore = new BasicCookieStore();
		}
		for (Map.Entry<String, String> cookieEntry : site.getCookies().entrySet()) {
			BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
			cookie.setDomain(site.getDomain());
			cookieStore.addCookie(cookie);
		}
		for (Map.Entry<String, Map<String, String>> domainEntry : site.getAllCookies().entrySet()) {
			for (Map.Entry<String, String> cookieEntry : domainEntry.getValue().entrySet()) {
				BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
				cookie.setDomain(domainEntry.getKey());
				cookieStore.addCookie(cookie);
			}
		}
		httpClientBuilder.setDefaultCookieStore(cookieStore);
	}

}
