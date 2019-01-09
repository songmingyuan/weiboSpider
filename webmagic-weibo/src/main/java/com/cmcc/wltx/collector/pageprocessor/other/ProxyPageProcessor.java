package com.cmcc.wltx.collector.pageprocessor.other;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.exception.PageProcessException;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.scheduler.QueueScheduler;
import com.cmcc.wltx.collector.service.ServiceFactory;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.MyNullPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public class ProxyPageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ProxyPageProcessor.class);
	public static final String URL_CHECK = "http://www.baidu.com/";
	private static final String REQUEST_EXTRA_TIME = "time";
	private static final String REQUEST_EXTRA_EFFECTIVENESS = "effectiveness";
	private static final String REQUEST_EXTRA_TREND = "trend";
	private final Pattern PA_PROXY = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})");
	private volatile long time = System.currentTimeMillis();
	private final Set<HttpHost> proxys;
	public ProxyPageProcessor(MySite site, Set<HttpHost> proxys) {
		super(site);
		this.proxys = proxys;
	}

	@Override
	public void process(Page page) throws PageProcessException {
		Request request = page.getRequest();
		int type = (Integer) request.getExtra(ConstantsHome.REQUEST_EXTRA_TYPE);
		switch (type) {
		case 0:
			Integer effectiveness = (Integer) request
					.getExtra(REQUEST_EXTRA_EFFECTIVENESS);
			Integer trend = (Integer) request.getExtra(REQUEST_EXTRA_TREND);
			String rawText = page.getRawText();
			if (rawText.contains("百度一下") && rawText.contains("你就知道")
					&& rawText.contains("京ICP证030173号")) {
				//验证成功
				// 如果效力已到底限（再降就要丢弃了）且趋向不算太坏，则效力额外+1
				if (effectiveness == -128 && trend > -3) {
					effectiveness++;
				}
				// 效力正常+1
				if (effectiveness < 127) {
					effectiveness++;
				}
				// 趋向如坏变好，如好更好（+1）
				if (trend < 0) {
					trend = 1;
				} else if (trend < 127) {
					trend++;
				}
			} else {
				// 验证失败
				logger.warn("unexpected page content - {}", request.toString());
				logger.warn(rawText);
				// 如效力已到底限则丢弃该代理
				if (effectiveness <= -128) {
					return;
				}
				// 效力正常-1
				effectiveness--;
				// 趋向如好变坏，如坏更坏（-1）
				if (trend > 0) {
					trend = -1;
				} else if (trend > -128) {
					trend--;
				}
			}

			saveProxy(request, effectiveness, trend);
			break;
		case 1:
			processXicidailiPage(page);
			break;
		case 2:
			processKuaidailiPage(page);
			break;
		case 3:
			processKxdailiPage(page);
			break;
		case 4:
		case 8:
			processGoubanjiaPage(page, type);
			break;
		case 5:
			processNianshaoPage(page);
			break;
		case 6:
			processBaizhongsouPage(page);
			break;
		case 7:
		case 11:
		case 12:
			processHaoipPage(page, type);
			break;
		case 9:
			processHttpdailiPage(page);
			break;
		case 10:
			processHttpsdailiPage(page);
			break;
		case 13:
			processIp181Page(page);
			break;
		default:
			break;
		}
	}

	private void processIp181Page(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = "div.row table > tbody > tr";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		boolean first = true;
		for (Element row : rows) {
			if (first) {
				first = false;
				continue;
			}
			try {
				String host = row.child(0).ownText().trim();
				int port = Integer.parseInt(row.child(1).ownText().trim());
				HttpHost proxy = new HttpHost(host, port);
				if(isNewProxy(proxy)){
					page.addTargetRequest(createCheckRequest(proxy, null));
					logger.info("found new proxy - {}:{}", host, port);
				}
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
		}
	}

	private void processHttpsdailiPage(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText(), "http://www.httpsdaili.com/");
		String rowsCss = "#list > table > tbody > tr";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		for (Element row : rows) {
			String host = row.child(0).ownText().trim();
			int port = Integer.parseInt(row.child(1).ownText().trim());
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
		
		// 下一页
		String url = page.getRequest().getUrl();
		try {
			String base = "http://www.httpsdaili.com/?stype=1&page=";
			int pageNum = Integer.parseInt(url.substring(base.length())) + 1;
			if (pageNum <= 10) {
				addNaviRequest(page, base + pageNum, 10);
			}
		} catch (Exception e) {
			throw new PageProcessException("页面结构异常：没有找到下一页 - " + url);
		}
	}

	private void processHttpdailiPage(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = "#kb-wall-truth-list > li:nth-child(1) table > tbody > tr";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		boolean first = true;
		for (Element row : rows) {
			if (first) {
				first = false;
				continue;
			}
			try {
				String host = row.child(0).ownText().trim();
				int port = Integer.parseInt(row.child(1).ownText().trim());
				HttpHost proxy = new HttpHost(host, port);
				if(isNewProxy(proxy)){
					page.addTargetRequest(createCheckRequest(proxy, null));
					logger.info("found new proxy - {}:{}", host, port);
				}
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
		}
	}

	private void processHaoipPage(Page page, int type) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = null;
		switch (type) {
		case 7:
			rowsCss = "div.row";
			break;
		case 11:
		case 12:
			rowsCss = "body";
			break;
		default:
			break;
		}
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		String text = rows.get(0).text();
		Matcher ma = PA_PROXY.matcher(text);
		while (ma.find()) {
			String host = ma.group(1);
			int port = Integer.parseInt(ma.group(2));
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
	}

	private void processBaizhongsouPage(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = "div.daililist > table > tbody > tr > td:nth-child(1)";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		Pattern pa = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)");
		for (Element row : rows) {
			Matcher ma = pa.matcher(row.ownText());
			if (ma.find()) {
				String host = ma.group(1);
				int port = Integer.parseInt(ma.group(2));
				HttpHost proxy = new HttpHost(host, port);
				if(isNewProxy(proxy)){
					page.addTargetRequest(createCheckRequest(proxy, null));
					logger.info("found new proxy - {}:{}", host, port);
				}
			}
		}
	}

	private void processNianshaoPage(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = "table.table > tbody > tr";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		for (Element row : rows) {
			String host = row.child(0).ownText().trim();
			int port = Integer.parseInt(row.child(1).ownText().trim());
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
		
		// 下一页
		String url = page.getRequest().getUrl();
		try {
			int pageNum = Integer.parseInt(url.substring(29)) + 1;
			if (pageNum <= 10) {
				addNaviRequest(page, "http://www.nianshao.me/?page=" + pageNum, 5);
			}
		} catch (Exception e) {
			throw new PageProcessException("页面结构异常：没有找到下一页 - " + url);
		}
	}

	private void processGoubanjiaPage(Page page, int type) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = null;
		switch (type) {
		case 4:
			rowsCss = "#list > table > tbody > tr";
			break;
		case 8:
			rowsCss = "#proxylisttable > tbody > tr";
			break;
		default:
			break;
		}
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		StringBuilder sb = new StringBuilder();
		for (Element row : rows) {
			Elements children = row.child(0).children();
			int port = Integer.MIN_VALUE;
			for (Element child : children) {
				String style = child.attr("style");
				if (style.contains("none")) {
					continue;
				}
				Set<String> classNames = child.classNames();
				if (classNames.contains("port")) {
					port = Integer.parseInt(child.text().trim());
					break;
				}
				sb.append(child.text().trim());
			}
			
			String host = sb.toString();
			if (port == Integer.MIN_VALUE) {
				port = Integer.parseInt(row.child(1).ownText().trim());
			}
			
			sb.setLength(0);
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
	}

	private void processKxdailiPage(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText());
		String rowsCss = "table.ui > tbody > tr";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		for (Element row : rows) {
			String host = row.child(0).ownText().trim();
			int port = Integer.parseInt(row.child(1).ownText().trim());
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
		
		// 下一页
		String url = page.getRequest().getUrl();
		try {
			int pageNum = Integer.parseInt(url.substring(33, url.lastIndexOf('.'))) + 1;
			if (pageNum <= 10) {
				addNaviRequest(page, "http://www.kxdaili.com/dailiip/1/" + pageNum + ".html", 3);
			}
		} catch (Exception e) {
			throw new PageProcessException("页面结构异常：没有找到下一页 - " + url);
		}
	}

	private void processKuaidailiPage(Page page) throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText(), "http://www.kuaidaili.com/");
		String rowsCss = "#list > table > tbody > tr";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}
		
		for (Element row : rows) {
			String host = row.child(0).ownText().trim();
			int port = Integer.parseInt(row.child(1).ownText().trim());
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
		
		// 下一页
		String url = page.getRequest().getUrl();
		try {
			String base = "http://www.kuaidaili.com/free/inha/";
			int pageNum = Integer.parseInt(url.substring(base.length())) + 1;
			if (pageNum <= 10) {
				addNaviRequest(page, base + pageNum, 2);
			}
		} catch (Exception e) {
			throw new PageProcessException("页面结构异常：没有找到下一页 - " + url);
		}
	}

	private void processXicidailiPage(Page page)
			throws PageProcessException {
		Document doc = Jsoup.parse(page.getRawText(), "http://www.xicidaili.com/");
		String rowsCss = "#ip_list > tbody:nth-child(1) > tr[class]";
		Elements rows = doc.select(rowsCss);
		if (rows.size() == 0) {
			throw new PageProcessException("页面结构异常：没有找到 " + rowsCss);
		}

		for (Element row : rows) {
			String host = row.child(1).ownText().trim();
			int port = Integer.parseInt(row.child(2).ownText().trim());
			HttpHost proxy = new HttpHost(host, port);
			if(isNewProxy(proxy)){
				page.addTargetRequest(createCheckRequest(proxy, null));
				logger.info("found new proxy - {}:{}", host, port);
			}
		}
		
		// 下一页
		String url = page.getRequest().getUrl();
		try {
			String base = "http://www.xicidaili.com/nn/";
			int pageNum = Integer.parseInt(url.substring(base.length())) + 1;
			if (pageNum <= 10) {
				addNaviRequest(page, base + pageNum, 1);
			}
		} catch (Exception e) {
			throw new PageProcessException("页面结构异常：没有找到下一页 - " + url);
		}
	}

	private boolean isNewProxy(HttpHost proxy) {
		synchronized (proxys) {
			return proxys.add(proxy);
		}
	}

	private static Request createCheckRequest(HttpHost proxy, Long createTime,
			int effectiveness, int trend) {
		return new Request(URL_CHECK)
				.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY, proxy)
				.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 0)
				.putExtra(REQUEST_EXTRA_EFFECTIVENESS, effectiveness)
				.putExtra(REQUEST_EXTRA_TREND, trend)
				.putExtra(REQUEST_EXTRA_TIME, createTime);
	}

	private static Request createCheckRequest(HttpHost proxy, Long createTime) {
		return createCheckRequest(proxy, createTime, -128, -1);
	}
	
	private void addNaviRequest(Page page, String url, int type) {
		page.addTargetRequest(new Request(url).putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, type));
		page.addTargetRequest(new Request("http://www.goubanjia.com/free/gngn/index.shtml").putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 4));
		if (over(300000l)) {
			page.addTargetRequest(new Request("http://ip.baizhongsou.com/default.aspx").putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 6));
			page.addTargetRequest(new Request("http://www.haoip.cc/tiqu.htm").putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 7));
			page.addTargetRequest(new Request("http://ip.izmoney.com/free/china-high.html").putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 8));
			page.addTargetRequest(new Request("http://www.httpdaili.com/mfdl/").putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 9));
			page.addTargetRequest(new Request("http://www.ip181.com/").putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 13));
		}
	}

	private boolean over(long interval) {
		synchronized (REQUEST_EXTRA_TIME) {
			long c = System.currentTimeMillis();
			if (c - time > interval) {
				time = c;
				return true;
			}
			return false;
		}
	}

	@Override
	public void onError(Request request, Throwable e) {
		logger.warn("process request failed - {}", request.toString());
		int type = (Integer) request.getExtra(ConstantsHome.REQUEST_EXTRA_TYPE);
		if (0 != type) {
			return;
		}
		Integer effectiveness = (Integer) request.getExtra(REQUEST_EXTRA_EFFECTIVENESS);
		if (effectiveness <= -128) {
			return;
		}
		effectiveness--;
		Integer trend = (Integer) request.getExtra(REQUEST_EXTRA_TREND);
		if (trend > 0) {
			trend = -1;
		} else if (trend > -128) {
			trend--;
		}
		
		saveProxy(request, effectiveness, trend);
	}
	
	private void saveProxy(Request request, int effectiveness, int trend) {
		HttpHost proxy = (HttpHost) request
				.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY);
		Long time = (Long) request.getExtra(REQUEST_EXTRA_TIME);
		if (null == time) {
			time = System.currentTimeMillis();
		}
		articlesLogger.info("('{}',{},2,{},{},{}),", proxy.getHostName(),
				proxy.getPort(), time, effectiveness, trend);
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			logger.error("缺少爬虫ID");
			return;
		}
		MyDataSource.init();
		try {
			launch(args);
		} finally {
			MyDataSource.destroy();
		}
	}

	private static void launch(String[] spiderIds) {
		MySpider.test = spiderIds[spiderIds.length - 1]
				.startsWith(MySpider.PREFIX_ID_TEST);
		
		Set<HttpHost> proxys = new HashSet<HttpHost>();
		List<Request> requests = new ArrayList<Request>();
		if (!MySpider.test) {
			Connection conn = MyDataSource.connect();
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				rs = stmt
						.executeQuery("SELECT c_host,c_port,c_time_create,c_effectiveness,c_trend FROM t_config_proxy where c_status = 2");
				while (rs.next()) {
					HttpHost proxy = new HttpHost(rs.getString("c_host"),
							rs.getInt("c_port"));
					proxys.add(proxy);
					long time = rs.getLong("c_time_create");
					int effectiveness = rs.getInt("c_effectiveness");
					int trend = rs.getInt("c_trend");
					requests.add(createCheckRequest(proxy, time, effectiveness,
							trend));
				}
			} catch (SQLException e) {
				throw new Error("代理配置获取失败", e);
			} finally {
				MyDataSource.release(rs, stmt, conn);
			}
		}

		// 获取配置
		SpiderService spiderService = ServiceFactory.getSpiderService();
		MySite site;
		try {
			site = spiderService.buildMySite(spiderIds);
		} catch (ServiceException e) {
			logger.error("spider config load error", e);
			return;
		}

		// 创建爬虫
		BasicPageProcessor pageProcessor = new ProxyPageProcessor(site,
				proxys);
		MySpider spider = MySpider.create(pageProcessor, null,
				new QueueScheduler(), MySpider.test ? 0 : 1,
				new MyNullPipeline());

		// 验证已有的代理
		for (Request request : requests) {
			spider.addRequest(request);
		}
		// 采集新代理
		Request[] request = {
				new Request("http://www.xicidaili.com/nn/1").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 1),
				new Request("http://www.kuaidaili.com/free/inha/1").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 2),
				new Request("http://www.kxdaili.com/dailiip/1/1.html")
						.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 3),
				new Request("http://www.goubanjia.com/free/gngn/index.shtml")
						.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 4),
				new Request("http://www.nianshao.me/?page=1").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 5),
				new Request("http://ip.baizhongsou.com/default.aspx").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 6),
				new Request("http://www.haoip.cc/tiqu.htm").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 7),
				new Request("http://ip.izmoney.com/free/china-high.html")
						.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 8),
				new Request("http://www.httpdaili.com/mfdl/").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 9),
				new Request("http://www.httpsdaili.com/?stype=1&page=1")
						.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 10),
				new Request(
						"http://www.66ip.cn/nmtq.php?getnum=800&isp=0&anonymoustype=3&start=&ports=&export=&ipaddress=&area=1&proxytype=2&api=66ip")
						.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 11),
				new Request(
						"http://www.89ip.cn/api/?&tqsl=1000&sxa=&sxb=&tta=&ports=&ktip=&cf=1")
						.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 12),
				new Request("http://www.ip181.com/").putExtra(
						ConstantsHome.REQUEST_EXTRA_TYPE, 13) };
		spider.addRequest(request);

		// 启动
		try {
			spider.run();
		} finally {
			if (null != spider) {
				spider.close();
			}
		}
	}

}
