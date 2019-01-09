package com.cmcc.wltx.collector.pageprocessor.weibo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.utils.HttpConstant;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.json.JSException;
import com.cmcc.json.JSObject;
import com.cmcc.json.JSUtils;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.scheduler.QueueScheduler;
import com.cmcc.wltx.collector.service.ServiceFactory;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.downloader.MyHttpClientDownloader;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.MyNullPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

/**
 * @author mingyuan.song 采集策略变更
 *
 */
public class WeiboVisitorCookiePageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(WeiboVisitorCookiePageProcessor.class);

	public WeiboVisitorCookiePageProcessor(MySite site) {
		super(site);
	}

	@Override
	public void process(Page page) throws PageProcessException {
		int type = (Integer) page.getRequest().getExtra(ConstantsHome.REQUEST_EXTRA_TYPE);
		switch (type) {
		case 1:
			processGenvisitorPage(page);
			break;
		case 2:
			processVisitorPage(page);
			break;
		default:
			break;
		}
	}

	private void processVisitorPage(Page page) throws PageProcessException {
		String rawText = page.getRawText();
		Pattern pa = Pattern.compile("window\\.cross_domain && cross_domain\\((\\{.+\\})\\);");
		Matcher ma = pa.matcher(rawText);
		if (!ma.matches()) {
			throw new PageProcessException("cross_domain正则不匹配");
		}
		try {
			JSObject data = JSUtils.createJSObject(ma.group(1)).getNotNullJSObject("data");
			String sub = data.getNotNullString("sub").trim();
			String subp = data.getNotNullString("subp").trim();
			String cookie = "SUB=" + sub + "; SUBP=" + subp;
			if (sub.length() == 0 || subp.length() == 0) {
				throw new PageProcessException("cookie异常 - " + cookie);
			}
			logger.info("采集的访客cookie - {}", cookie);
			saveCookie(cookie);
		} catch (JSException e) {
			throw new PageProcessException("cookie获取失败", e);
		}
	}

	private void saveCookie(String cookie) {
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("insert into t_cookie_weibo_visitor (c_cookie, c_time_create) values (?,?)");
			stmt.setString(1, cookie);
			stmt.setLong(2, System.currentTimeMillis());
			int res = stmt.executeUpdate();
			if (1 != res) {
				logger.warn("cookie保存失败 - " + cookie);
			}
			logger.info("saveCookie 执行成功");
		} catch (SQLException e) {
			logger.warn("cookie保存失败 - " + cookie, e);
		} finally {
			MyDataSource.release(stmt, conn);
		}
	}

	private void processGenvisitorPage(Page page) throws PageProcessException {
		String rawText = page.getRawText();
		Pattern pa = Pattern.compile("window\\.gen_callback && gen_callback\\((\\{.+\\})\\);");
		Matcher ma = pa.matcher(rawText);
		if (!ma.matches()) {
			throw new PageProcessException("gen_callback正则不匹配");
		}
		String tid;
		try {
			tid = JSUtils.createJSObject(ma.group(1)).getNotNullJSObject("data").getNotNullString("tid").trim();
		} catch (JSException e) {
			throw new PageProcessException("tid获取失败", e);
		}

		if (null == tid || tid.length() == 0) {
			throw new PageProcessException("tid获取失败");
		}

		String tid_u;
		try {
			tid_u = URLEncoder.encode(tid, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new PageProcessException("URLEncode失败", e);
		}
		Request request = new Request("https://passport.weibo.com/visitor/visitor?a=incarnate&t=" + tid_u
				+ "&w=3&c=100&gc=&cb=cross_domain&from=weibo&_rand=" + Math.random());
		request.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 2);
		request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY,
				page.getRequest().getExtra(ConstantsHome.REQUEST_EXTRA_PROXY));
		if (null != page.getRequest().getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE)) {
			request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE,
					page.getRequest().getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE));
		}
		page.addTargetRequest(request);
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("缺少爬虫ID");
			return;
		}
		if (ConstantsHome.BLOCK.isFile()) {
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
		String[] newSpiderIds = new String[1];
		String spiderId = spiderIds[0];
		// 0：本机ip；1：内部代理；2：免费外部代理；3：付费外部代理；
		int proxyType = 0;
		if (spiderIds.length == 1) {

		} else if (spiderIds.length == 2) {
			String proxyTypeId = spiderIds[1];
			if (StringUtils.isNotBlank(proxyTypeId)) {
				proxyType = Integer.valueOf(proxyTypeId);
			}
		} else {
			logger.error("args number is error");
			return;
		}
		newSpiderIds[0] = spiderId;
		// 获取配置
		SpiderService spiderService = ServiceFactory.getSpiderService();
		MySite site;
		try {
			site = spiderService.buildMySiteNew(newSpiderIds);
		} catch (ServiceException e) {
			logger.error("spider config load error", e);
			return;
		}

		MySpider spider = null;
		try {
			// 创建爬虫
			spider = MySpider.create(new WeiboVisitorCookiePageProcessor(site), null, new QueueScheduler(), proxyType,
					new MyNullPipeline());
			do {
				Request request = new Request("https://passport.weibo.com/visitor/genvisitor");
				request.setMethod(HttpConstant.Method.POST);
				NameValuePair[] params = new BasicNameValuePair[2];
				params[0] = new BasicNameValuePair("cb", "gen_callback");
				params[1] = new BasicNameValuePair("fp", "{}");
				request.putExtra(ConstantsHome.NAME_VALUE_PAIR, params);
				request.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, 1);
				// 标注代理类型
				request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE, proxyType);
				spider.addRequest(request);

				spider.run();

				if (ConstantsHome.BLOCK.exists()) {
					break;
				}
				if (site.getTimeInterval() > 0) {
					logger.info("interval sleeping...");
					try {
						Thread.sleep(site.getTimeInterval());
					} catch (InterruptedException e) {
						logger.info("interval sleep interrupted", e);
					}
					if (ConstantsHome.BLOCK.exists()) {
						break;
					}
				}

				if (proxyType == 2) {
					((MyHttpClientDownloader) spider.getDownloader()).initProxy(proxyType);
				}
			} while (true);
		} finally {
			if (null != spider) {
				spider.close();
			}
		}
	}
}
