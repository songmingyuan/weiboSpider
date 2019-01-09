package com.cmcc.wltx.collector.spider.mywebmagic.downloader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.pageprocessor.other.ProxyPageProcessor;
import com.cmcc.wltx.collector.spider.util.TaskUtils;
import com.google.common.collect.Sets;

public class MyHttpClientDownloader extends AbstractDownloader {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MyHttpClientDownloader.class);
	private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();
	private MyHttpClientGenerator httpClientGenerator = new MyHttpClientGenerator();
	private final List<HttpHost> proxys = new ArrayList<HttpHost>();
	private final String INNER_PROXY_PREFIX = "172.17.18.";
	private int proxySize;
	private final List<InetAddress> localHosts;
	private final int localHostSize;

	public MyHttpClientDownloader(String ips, int proxyType) {
		super();
		if (null != ips && ips.length() != 0) {
			String[] split = ips.split(";");
			localHosts = new ArrayList<InetAddress>(split.length);
			for (String ip : split) {
				try {
					localHosts.add(InetAddress.getByName(ip));
				} catch (UnknownHostException e) {
					throw new Error(e);
				}
			}
			localHostSize = localHosts.size();
		} else {
			localHosts = null;
			localHostSize = 0;
		}

		initProxy(proxyType);
	}

	public void updateProxyByLocalNotice(int proxyType) {
		if (!ConstantsHome.UPDATE_PROXY.exists()) {
			return;
		}

		if (ConstantsHome.UPDATE_PROXY.isFile()) {
			String content;
			try {
				content = FileUtils.readFileToString(ConstantsHome.UPDATE_PROXY);
			} catch (IOException e) {
				logger.warn("代理更新通知读取失败", e);
				return;
			}
			try {
				proxyType = Integer.parseInt(content);
			} catch (NumberFormatException e) {
				logger.warn("代理更新通知内容异常 - " + content, e);
				return;
			}
		}

		ConstantsHome.UPDATE_PROXY.delete();
		initProxy(proxyType);
	}

	public void initProxy(int proxyType) {
		logger.info("init proxy - {}", proxyType);
		proxys.clear();
		proxySize = 0;
		if (proxyType < 1 || proxyType > 7) {
			return;
		}

		Connection conn = MyDataSource.connect();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			switch (proxyType) {
			case 1:
				rs = stmt.executeQuery("SELECT c_host,c_port FROM t_config_proxy where c_status = 1");
				break;
			case 2:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_status = 2 and c_effectiveness > -80 and c_trend > 9 and c_time_create < "
								+ (System.currentTimeMillis() - 86400000l));
				break;
			case 3:
				break;
			case 4:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_effectiveness = 127 and (c_trend = 127 or c_trend = 0)");
				break;
			case 5:
				break;
			case 6:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_status = 2 and c_effectiveness > -80 and c_trend > 9 and c_time_create < "
								+ (System.currentTimeMillis() - 86400000l));
				break;
			case 7:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_effectiveness > -80 and (c_trend > 9 or c_trend = 0) and c_time_create < "
								+ (System.currentTimeMillis() - 86400000l));
				break;
			default:
				break;
			}

			Set<String> hosts = new HashSet<String>();
			if (proxyType == 5 && localHosts != null) {
				for (InetAddress address : localHosts) {
					String host = address.getHostAddress();
					hosts.add(host);
					proxys.add(new HttpHost(host, 9667));
				}
			}
			if (proxyType == 6) {
				proxys.add(new HttpHost("172.17.18.2", 9667));
				proxys.add(new HttpHost("172.17.18.11", 9667));
				proxys.add(new HttpHost("172.17.18.28", 9667));
				proxys.add(new HttpHost("172.17.18.33", 9667));
				proxys.add(new HttpHost("172.17.18.43", 9667));
				proxys.add(new HttpHost("172.17.18.54", 9667));
				proxys.add(new HttpHost("172.17.18.66", 9667));
				proxys.add(new HttpHost("172.17.18.77", 9667));
				proxys.add(new HttpHost("172.17.18.113", 9667));
				proxys.add(new HttpHost("172.17.18.123", 9667));
				proxys.add(new HttpHost("172.17.18.149", 9667));
				proxys.add(new HttpHost("172.17.18.159", 9667));
				proxys.add(new HttpHost("172.17.18.163", 9667));
				proxys.add(new HttpHost("172.17.18.169", 9667));
				proxys.add(new HttpHost("172.17.18.171", 9667));
				proxys.add(new HttpHost("172.17.18.174", 9667));
				proxys.add(new HttpHost("172.17.18.180", 9667));
				proxys.add(new HttpHost("172.17.18.185", 9667));
				proxys.add(new HttpHost("172.17.18.186", 9667));
				proxys.add(new HttpHost("172.17.18.192", 9667));
			}
			if (null != rs) {
				while (rs.next()) {
					String host = rs.getString("c_host");
					if (hosts.add(host)) {
						proxys.add(new HttpHost(host, rs.getInt("c_port")));
					}
				}
			}
			proxySize = proxys.size();
		} catch (SQLException e) {
			throw new Error("代理配置获取异常", e);
		} finally {
			MyDataSource.release(rs, stmt, conn);
		}
	}

	public MyHttpClientDownloader() {
		this(null, 0);
	}

	/**
	 * 域名单例模式，优点：适用于二次登陆的请求
	 * 
	 * @param site
	 * @return
	 */
	private CloseableHttpClient getHttpClient(Site site) {
		if (site == null) {
			return httpClientGenerator.getClient(null);
		}
		String domain = site.getDomain();
		CloseableHttpClient httpClient = httpClients.get(domain);
		if (httpClient == null) {
			synchronized (this) {
				httpClient = httpClients.get(domain);
				if (httpClient == null) {
					httpClient = httpClientGenerator.getClient(site);
					httpClients.put(domain, httpClient);
				}
			}
		}
		return httpClient;
	}

	/**
	 * 新方法，每个请求都需要验证代理和用户
	 * 
	 * 针对需要二次登陆的请求不适用，每次构建新的对象
	 * 
	 * @param site
	 * @return
	 */
	private CloseableHttpClient getHttpClientNew(Site site, HttpHost httpProxy) {
		if (site == null) {
			return httpClientGenerator.getClient(null);
		}
		return httpClientGenerator.getClientNew(site, httpProxy);
	}

	@SuppressWarnings("unchecked")
	public Page superDownload(Request request, Task task) {
		Site site = null;
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		String charset = null;
		Map<String, String> headers = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			// 修改页面编码
			Object charsetParam = request.getExtra(ConstantsHome.REQUEST_EXTRA_CHARSET);
			if (null != charsetParam) {
				charset = String.valueOf(charsetParam);
			} else {
				charset = site.getCharset();
			}
			headers = site.getHeaders();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		Object requestHeaders = request.getExtra(ConstantsHome.REQUEST_EXTRA_HEADERS);
		if (null != requestHeaders) {
			if (null == headers) {
				headers = (Map<String, String>) requestHeaders;
			} else {
				headers.putAll((Map<String, String>) requestHeaders);
			}
		}
		String url = request.getUrl();
		logger.info("downloading page {}", url);
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		HttpUriRequest httpUriRequest = getHttpUriRequest(request, site, headers);
		HttpHost proxy = (HttpHost) request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY);
		String hostName;
		int port;
		int proxyType;
		if (null == proxy) {
			hostName = "";
			port = 0;
			proxyType = 0;
		} else {
			hostName = proxy.getHostName();
			port = proxy.getPort();
			proxyType = hostName.startsWith(INNER_PROXY_PREFIX) ? 1 : 2;
		}
		try {
			HttpClientContext context = HttpClientContext.create();
			long start = System.currentTimeMillis();
			httpResponse = getHttpClient(site).execute(httpUriRequest, context);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			logger.info("[{}]DRS~{}~{}~{}~{}", url, statusCode, proxyType, null == proxy ? "" : hostName + ':' + port,
					System.currentTimeMillis() - start);

			if (!statusAccept(acceptStatCode, statusCode)) {
				return null;
			}
			if (!isNormalPage(httpResponse, url)) {
				return null;
			}
			Page page = handleResponse(request, charset, httpResponse, task);
			onSuccess(request);
			return page;
		} catch (IOException e) {
			if (site.getCycleRetryTimes() > 0) {
				Page p = addToCycleRetry(request, site);
				if (null != p) {
					logger.info("cycle retry - " + request.toString(), e);
					return p;
				}
			}
			if (ProxyPageProcessor.URL_CHECK != url && 2 == proxyType) {
				// 如果用的是外网代理，换代理重新访问，直到换到内网代理还是失败的话就过掉
				request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY, null);
				request.putExtra(Request.CYCLE_TRIED_TIMES, null);
				Page p = new Page();
				p.addTargetRequest(request.setPriority(0));
				p.setNeedCycleRetry(true);
				logger.info("suspicious proxy[" + hostName + ':' + port + "] - " + url, e);
				return p;
			}
			logger.warn("download page error - " + request.toString(), e);
			onError(request);
			return null;
		} finally {
			request.putExtra(Request.STATUS_CODE, statusCode);
			try {
				if (httpResponse != null) {
					// ensure the connection is released back to pool
					// EntityUtils.consume(httpResponse.getEntity());
					httpResponse.close();
				}
			} catch (IOException e) {
				logger.warn("close response fail", e);
			}
		}
	}

	/**
	 * 新的下载方法
	 * 
	 * @param request
	 * @param task
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Page superDownloadNew(Request request, Task task) {
		Site site = null;
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		String charset = null;
		Map<String, String> headers = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			// 修改页面编码
			Object charsetParam = request.getExtra(ConstantsHome.REQUEST_EXTRA_CHARSET);
			if (null != charsetParam) {
				charset = String.valueOf(charsetParam);
			} else {
				charset = site.getCharset();
			}
			headers = site.getHeaders();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		Object requestHeaders = request.getExtra(ConstantsHome.REQUEST_EXTRA_HEADERS);
		if (null != requestHeaders) {
			if (null == headers) {
				headers = (Map<String, String>) requestHeaders;
			} else {
				headers.putAll((Map<String, String>) requestHeaders);
			}
		}
		String url = request.getUrl();
		logger.info("downloading page {}", url);
		CloseableHttpResponse httpResponse = null;
		CloseableHttpClient httpClient = null;
		int statusCode = 0;
		String baseProxyType = null;// 基础代理类型
		if (null != request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE)) {
			baseProxyType = request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE).toString();
		}
		HttpUriRequest httpUriRequest = null;
		if (StringUtils.isNotBlank(baseProxyType) && "3".equals(baseProxyType)) {
			httpUriRequest = getHttpUriRequestNew(request, site, headers);
		} else {
			httpUriRequest = getHttpUriRequest(request, site, headers);
		}
		HttpHost proxy = (HttpHost) request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY);
		int proxyChange = 0;// 是否切换免费代理
		if (null != request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE)) {
			proxyChange = Integer.valueOf(request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE).toString());
		}
		String hostName;
		int port;
		int proxyType;
		if (null == proxy) {
			hostName = "";
			port = 0;
			proxyType = 0;
		} else {
			hostName = proxy.getHostName();
			port = proxy.getPort();
			proxyType = hostName.startsWith(INNER_PROXY_PREFIX) ? 1 : 2;
		}
		try {
			HttpClientContext context = HttpClientContext.create();
			long start = System.currentTimeMillis();
			if (proxyType == 2) {// 外部代理
				if (StringUtils.isNotBlank(baseProxyType) && "3".equals(baseProxyType) && proxyChange == 0) {// 付费代理
					// 付费外部代理使用新的方法验证
					httpClient = getHttpClientNew(site, proxy);
				} else {
					httpClient = getHttpClientNew(site, null);
				}
			} else {
				httpClient = getHttpClient(site);
			}
			httpResponse = httpClient.execute(httpUriRequest, context);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			logger.info("[{}]DRS~{}~{}~{}~{}~{}~{}", url, statusCode, null == baseProxyType ? "" : baseProxyType,
					proxyType, proxyChange, null == proxy ? "" : hostName + ':' + port,
					System.currentTimeMillis() - start);

			if (!statusAccept(acceptStatCode, statusCode)) {
				// 正常返回，返回值不为200的
				return null;
			}
			if (!isNormalPage(httpResponse, url)) {
				return null;
			}
			Page page = handleResponse(request, charset, httpResponse, task);
			onSuccess(request);
			return page;
		} catch (IOException e) {
			// 重试时代理不变，现在默认不重试
			if (site.getCycleRetryTimes() > 0) {
				Page p = addToCycleRetry(request, site);
				if (null != p) {
					logger.info("cycle retry - " + request.toString(), e);
					return p;
				}
			}
			// 增加错误计数（防止无限重试）
			int num = 0;
			if (null != request.getExtra(ConstantsHome.REQUEST_EXTRA_RETRY_NUM)) {
				num = Integer.valueOf(request.getExtra(ConstantsHome.REQUEST_EXTRA_RETRY_NUM).toString());
			}
			if (num < 5) {
				num++;
				request.putExtra(ConstantsHome.REQUEST_EXTRA_RETRY_NUM, num);
				if (ProxyPageProcessor.URL_CHECK != url && 2 == proxyType) {
					// 如果用的是外网代理，换代理重新访问，直到换到内网代理还是失败的话就过掉
					request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY, null);
					request.putExtra(Request.CYCLE_TRIED_TIMES, null);
					request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE, null);
					Page p = new Page();
					p.addTargetRequest(request.setPriority(0));
					p.setNeedCycleRetry(true);
					logger.info("suspicious proxy[" + hostName + ':' + port + "] - " + url, e);
					return p;
				}
			}
			logger.warn("download page error - " + request.toString(), e);
			onError(request);
			return null;
		} finally {
			request.putExtra(Request.STATUS_CODE, statusCode);
			try {
				if (httpResponse != null) {
					httpResponse.close();
				}
				// if (httpClient != null) {
				// httpClient.close();
				// }
			} catch (IOException e) {
				logger.warn("close response fail", e);
			}
		}
	}

	private boolean isNormalPage(CloseableHttpResponse httpResponse, String url) {
		HttpEntity entity = httpResponse.getEntity();
		Header contentType = entity.getContentType();
		if (null != contentType) {
			String ct = contentType.getValue();
			if (null != ct && ct.length() != 0) {
				ct = ct.toLowerCase().trim();
				if (ct.startsWith("text/") || ct.contains("json") || ct.contains("javascript")) {
					return true;
				} else {
					logger.warn("unsupport Content-Type:{} - {}", ct, url);
					return false;
				}
			}
		}
		int maxLength = 1048576;
		if (entity.getContentLength() > maxLength) {
			logger.warn("content length over {} - {}", maxLength, url);
			return false;
		}
		Header lengthHeader = httpResponse.getFirstHeader("Content-Length");
		if (null != lengthHeader) {
			if (Long.parseLong(lengthHeader.getValue()) > maxLength) {
				logger.warn("content length over {} - {}", maxLength, url);
				return false;
			}
		}
		return true;
	}

	@Override
	public void setThread(int thread) {
		httpClientGenerator.setPoolSize(thread);
	}

	protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
		return acceptStatCode.contains(statusCode);
	}

	@Override
	public Page download(Request request, Task task) {
		Page page = superDownloadNew(request, task);
		if (page == null || page.isNeedCycleRetry()) {
			return page;
		}
		String rawText = page.getRawText();
		if (null == rawText || rawText.length() == 0) {
			logger.warn("rawText isEmpty - {}", request.getUrl());
			return null;
		}
		return page;
	}

	protected HttpUriRequest getHttpUriRequestNew(Request request, Site site, Map<String, String> headers) {
		RequestBuilder requestBuilder = selectRequestMethod(request).setUri(request.getUrl());
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout(site.getTimeOut()).setSocketTimeout(site.getTimeOut())
				.setConnectTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.BEST_MATCH);

		HttpHost proxy = (HttpHost) request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY);
		if (null != proxy) {
			boolean isLocalHost = false;
			if (localHostSize != 0) {
				for (InetAddress localhost : localHosts) {
					if (localhost.getHostAddress().equals(proxy.getHostName())) {
						requestConfigBuilder.setLocalAddress(localhost);
						isLocalHost = true;
						break;
					}
				}
			}
			if (!isLocalHost) {
				requestConfigBuilder.setProxy(proxy);
			}
		} else {
			Map<String, Object> proxyMap = TaskUtils.getValidPayProxy();
			int isChange = 0;// 是否切换免费代理
			if (proxyMap != null && proxyMap.size() == 2) {
				proxy = (HttpHost) proxyMap.get("proxy");

				boolean isLocalHost = false;
				if (localHostSize != 0) {
					for (InetAddress localhost : localHosts) {
						if (localhost.getHostAddress().equals(proxy.getHostName())) {
							requestConfigBuilder.setLocalAddress(localhost);
							isLocalHost = true;
							break;
						}
					}
				}
				if (!isLocalHost) {
					requestConfigBuilder.setProxy(proxy);
				}
			} else {
				if (proxySize != 0) {
					proxy = proxys.get(ThreadLocalRandom.current().nextInt(proxySize));
					if (null != proxy) {
						isChange = 1;
					}
					boolean isLocalHost = false;
					if (localHostSize != 0) {
						for (InetAddress localhost : localHosts) {
							if (localhost.getHostAddress().equals(proxy.getHostName())) {
								requestConfigBuilder.setLocalAddress(localhost);
								isLocalHost = true;
								break;
							}
						}
					}
					if (!isLocalHost) {
						requestConfigBuilder.setProxy(proxy);
					}
				} else {
					if (localHostSize != 0) {
						requestConfigBuilder
								.setLocalAddress(localHosts.get(ThreadLocalRandom.current().nextInt(localHostSize)));
					}
					if (site.getHttpProxyPool() != null && site.getHttpProxyPool().isEnable()) {
						proxy = site.getHttpProxyFromPool();
						requestConfigBuilder.setProxy(proxy);
						request.putExtra(Request.PROXY, proxy);
					} else if (site.getHttpProxy() != null) {
						proxy = site.getHttpProxy();
						requestConfigBuilder.setProxy(proxy);
						request.putExtra(Request.PROXY, proxy);
					}
				}
			}
			request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY, proxy);
			request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE, isChange);
		}
		requestBuilder.setConfig(requestConfigBuilder.build());
		return requestBuilder.build();
	}

	protected HttpUriRequest getHttpUriRequest(Request request, Site site, Map<String, String> headers) {
		RequestBuilder requestBuilder = selectRequestMethod(request).setUri(request.getUrl());
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout(site.getTimeOut()).setSocketTimeout(site.getTimeOut())
				.setConnectTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.BEST_MATCH);

		HttpHost proxy = (HttpHost) request.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY);
		if (null != proxy) {
			boolean isLocalHost = false;
			if (localHostSize != 0) {
				for (InetAddress localhost : localHosts) {
					if (localhost.getHostAddress().equals(proxy.getHostName())) {
						requestConfigBuilder.setLocalAddress(localhost);
						isLocalHost = true;
						break;
					}
				}
			}
			if (!isLocalHost) {
				requestConfigBuilder.setProxy(proxy);
			}
		} else {
			if (proxySize != 0) {
				proxy = proxys.get(ThreadLocalRandom.current().nextInt(proxySize));

				boolean isLocalHost = false;
				if (localHostSize != 0) {
					for (InetAddress localhost : localHosts) {
						if (localhost.getHostAddress().equals(proxy.getHostName())) {
							requestConfigBuilder.setLocalAddress(localhost);
							isLocalHost = true;
							break;
						}
					}
				}
				if (!isLocalHost) {
					requestConfigBuilder.setProxy(proxy);
				}
			} else {
				if (localHostSize != 0) {
					requestConfigBuilder
							.setLocalAddress(localHosts.get(ThreadLocalRandom.current().nextInt(localHostSize)));
				}
				if (site.getHttpProxyPool() != null && site.getHttpProxyPool().isEnable()) {
					proxy = site.getHttpProxyFromPool();
					requestConfigBuilder.setProxy(proxy);
					request.putExtra(Request.PROXY, proxy);
				} else if (site.getHttpProxy() != null) {
					proxy = site.getHttpProxy();
					requestConfigBuilder.setProxy(proxy);
					request.putExtra(Request.PROXY, proxy);
				}
			}
			request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY, proxy);
		}
		requestBuilder.setConfig(requestConfigBuilder.build());
		return requestBuilder.build();
	}

	protected RequestBuilder selectRequestMethod(Request request) {
		String method = request.getMethod();
		if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
			// default get
			return RequestBuilder.get();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
			RequestBuilder requestBuilder = RequestBuilder.post();
			@SuppressWarnings("unchecked")
			List<NameValuePair> params = (List<NameValuePair>) request.getExtra(ConstantsHome.ENTITY_FORM_UTF8);
			if (null != params && params.size() > 0) {
				requestBuilder.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			} else {
				NameValuePair[] nameValuePair = (NameValuePair[]) request.getExtra(ConstantsHome.NAME_VALUE_PAIR);
				if (nameValuePair != null && nameValuePair.length > 0) {
					requestBuilder.addParameters(nameValuePair);
				}
			}
			return requestBuilder;
		} else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
			return RequestBuilder.head();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
			return RequestBuilder.put();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
			return RequestBuilder.delete();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
			return RequestBuilder.trace();
		}
		throw new IllegalArgumentException("Illegal HTTP Method " + method);
	}

	protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task)
			throws IOException {
		String content = getContent(charset, httpResponse);
		Page page = new Page();
		page.setRawText(content);
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);
		page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		Header[] allHeaders = httpResponse.getAllHeaders();
		Map<String, String> map = new HashMap<String, String>();
		for (Header header : allHeaders) {
			String headerName = header.getName();
			String headerValue = header.getValue();
			String value = map.get(headerName);
			if (null == value) {
				map.put(headerName, headerValue);
			} else {
				map.put(headerName, value + "; " + headerValue);
			}
		}
		page.setHeaders(map);
		return page;
	}

	protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
		if (charset == null) {
			byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
			String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
			if (htmlCharset != null) {
				return new String(contentBytes, htmlCharset);
			} else {
				logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()",
						Charset.defaultCharset());
				return new String(contentBytes);
			}
		} else {
			return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
		}
	}

	protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
		String charset;
		// charset
		// 1、encoding in http header Content-Type
		Header contentType = httpResponse.getEntity().getContentType();
		if (null == contentType) {
			return null;
		}
		String value = contentType.getValue();
		charset = UrlUtils.getCharset(value);
		if (StringUtils.isNotBlank(charset)) {
			logger.debug("Auto get charset: {}", charset);
			return charset;
		}
		// use default charset to decode first time
		Charset defaultCharset = Charset.defaultCharset();
		String content = new String(contentBytes, defaultCharset.name());
		// 2、charset in meta
		if (StringUtils.isNotEmpty(content)) {
			Document document = Jsoup.parse(content);
			Elements links = document.select("meta");
			for (Element link : links) {
				// 2.1、html4.01 <meta http-equiv="Content-Type"
				// content="text/html; charset=UTF-8" />
				String metaContent = link.attr("content");
				String metaCharset = link.attr("charset");
				if (metaContent.indexOf("charset") != -1) {
					metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
					charset = metaContent.split("=")[1];
					break;
				}
				// 2.2、html5 <meta charset="UTF-8" />
				else if (StringUtils.isNotEmpty(metaCharset)) {
					charset = metaCharset;
					break;
				}
			}
		}
		logger.debug("Auto get charset: {}", charset);
		// 3、todo use tools as cpdetector for content decode
		return charset;
	}
}