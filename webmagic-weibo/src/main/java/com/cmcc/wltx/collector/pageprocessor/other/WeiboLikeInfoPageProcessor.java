package com.cmcc.wltx.collector.pageprocessor.other;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.spider.mywebmagic.MySiteBuilder;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.downloader.MyHttpClientDownloader;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.MyNullPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public class WeiboLikeInfoPageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(WeiboLikeInfoPageProcessor.class);
	private static final int REQUEST_TYPE_START = 0;
	private static final int REQUEST_TYPE_FALLS = 1;
	private static final int REQUEST_TYPE_NAVI = 2;
	private static final int REQUEST_TYPE_TARGET = 3;
	private static final String REQUEST_EXTRA_TASK = "task";
	private static final String REQUEST_EXTRA_TYPE = "requestType";
	private static final String REQUEST_EXTRA_CONTENT = "content";
	private static final String REQUEST_EXTRA_PAGEBAR = "pagebar";
	private static final String REQUEST_EXTRA_PAGE = "page";
	private static final String REQUEST_EXTRA_DATA_QUERYFIX = "data_queryfix";
	private static final String REQUEST_EXTRA_PAGE_ID = "page_id";
	private static final String URL_PREFIX = "http://weibo.com/";
	private static final String URL_SUFFIX = "?is_all=1";
	private static final String URL_TEMPLATE_LIKE = "http://weibo.com/aj/v6/like/big?ajwvr=6&mid={mid}&page=1&__rnd=";

	public WeiboLikeInfoPageProcessor(MySite site) {
		super(site);
	}

	@Override
	public void process(Page page) throws PageProcessException {
		int type = (Integer) page.getRequest().getExtra(REQUEST_EXTRA_TYPE);
		switch (type) {
		case REQUEST_TYPE_START:
			processNaviPage(page, type, true);
			break;
		case REQUEST_TYPE_NAVI:
			processNaviPage(page, type, false);
			break;
		case REQUEST_TYPE_FALLS:
			processFallsPage(page);
			break;
		case REQUEST_TYPE_TARGET:
			processTargetPage(page);
			break;
		default:
			break;
		}
	}

	private void processFallsPage(Page page) throws PageProcessException {
		String rawText = page.getRawText();
		JSONObject jp = new JSONObject(rawText);
		String html = jp.getString("data");
		Document doc = Jsoup.parse(html);
		processWeiboListPage(page, doc, false);
	}

	private void processTargetPage(Page page) {
		Request currentRequest = page.getRequest();
		String content = (String) currentRequest
				.getExtra(REQUEST_EXTRA_CONTENT);
		String rawText = page.getRawText();
		JSONObject jp = new JSONObject(rawText);
		JSONObject data = jp.getJSONObject("data");
		JSONObject p = data.getJSONObject("page");
		int pagenum = p.getInt("pagenum");
		int totalpage = p.getInt("totalpage");
		if (pagenum < totalpage) {
			Request request = new Request(currentRequest.getUrl().replaceFirst(
					"&page=\\d+", "&page=" + (pagenum + 1)));
			request.putExtra(REQUEST_EXTRA_TYPE, REQUEST_TYPE_TARGET);
			request.putExtra(REQUEST_EXTRA_TASK,
					currentRequest.getExtra(REQUEST_EXTRA_TASK));
			request.putExtra(REQUEST_EXTRA_CONTENT,
					currentRequest.getExtra(REQUEST_EXTRA_CONTENT));
			page.addTargetRequest(request);
		}

		String html = data.getString("html");
		Document doc = Jsoup.parse(html);
		Elements liEles = doc.select("ul.emotion_list > li");
		int size = liEles.size();
		for (int i = 0; i < size; i++) {
			Element liEle = liEles.get(i);
			String uid = liEle.attr("uid");
			String uname = liEle.child(0).child(0).attr("title");
			articlesLogger.info("{}\t{}\t{}\t{}",
					(String) currentRequest.getExtra(REQUEST_EXTRA_TASK),
					content, uid, uname);
		}
	}

	private void processNaviPage(Page page, int requestType, boolean isStart)
			throws PageProcessException {
		String rawText = page.getRawText();

		if (isStart) {
			String page_id_head = "$CONFIG['page_id']='";
			int indexOfPageId = rawText.indexOf(page_id_head)
					+ page_id_head.length();
			if (-1 == indexOfPageId) {
				throw new PageProcessException("indexOfPageId=" + indexOfPageId);
			}
			page.getRequest().putExtra(
					REQUEST_EXTRA_PAGE_ID,
					rawText.substring(indexOfPageId,
							rawText.indexOf('\'', indexOfPageId)));
		}

		int indexFMViewScript = rawText
				.indexOf("<script>FM.view({\"ns\":\"pl.content.homeFeed.index\",\"domid\":\"Pl_Official_MyProfileFeed__");
		if (-1 == indexFMViewScript) {
			throw new PageProcessException("indexFMViewScript=" + indexFMViewScript);
		}
		int beginIndex = indexFMViewScript + 16;
		int endIndex = rawText.indexOf(")</script>", indexFMViewScript);

		JSONObject jp = new JSONObject(rawText.substring(beginIndex, endIndex));
		Document doc = Jsoup.parse(jp.getString("html"));
		if (isStart) {
			String data_queryfix = doc.child(0).child(1).child(0).child(0)
					.attr("data-queryfix");
			page.getRequest().putExtra(REQUEST_EXTRA_DATA_QUERYFIX,
					data_queryfix);
		}
		processWeiboListPage(page, doc, isStart);
	}

	private void processWeiboListPage(Page page, Document doc, boolean isStart)
			throws PageProcessException {
		Elements linkEles = doc
				.select("div.WB_cardwrap.WB_feed_type.S_bg2 > div.WB_feed_detail.clearfix > div.WB_detail > div.WB_from > a.S_txt2:nth-child(1)");
		Elements contentEles = doc
				.select("div.WB_cardwrap.WB_feed_type.S_bg2 > div.WB_feed_detail.clearfix > div.WB_detail > div.WB_text");
		int size = linkEles.size();
		if (size == 0) {
			logger.warn("当前页面上的微博数量为0");
			return;
		}
		if (contentEles.size() != size) {
			throw new PageProcessException("微博链接与内容数量不一致！");
		}
		Elements detailEles = null;
		if (isStart) {
			detailEles = doc
					.select("div.WB_cardwrap.WB_feed_type.S_bg2 > div.WB_feed_detail.clearfix > div.WB_detail");
			if (detailEles.size() != size) {
				throw new PageProcessException("微博链接与微博数量不一致！");
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		String today = sdf.format(calendar.getTime());
		calendar.add(Calendar.DATE, -1);
		String yesterday = sdf.format(calendar.getTime());

		Request currentRequest = page.getRequest();
		boolean haveMore = true;
		for (int i = 0; i < size; i++) {
			Element linkEle = linkEles.get(i);
			String title = linkEle.attr("title");
			// 只采集当天的
			/*if (!title.startsWith(today)) {
				if (null != detailEles) {
					Elements W_icon_feedpin = detailEles.get(i).select(
							"span.W_icon_feedpin");
					if (W_icon_feedpin.size() != 0) {
						if (W_icon_feedpin.get(0).ownText().contains("置顶")) {
							continue;
						}
					}
				}
				logger.info("no more weibo published on today.");
				haveMore = false;
				break;
			}*/
			// 只采集前一天的
			if (title.startsWith(today)) {
				continue;
			}
			if (!title.startsWith(yesterday)) {
				if (null != detailEles) {
					Elements W_icon_feedpin = detailEles.get(i).select(
							"span.W_icon_feedpin");
					if (W_icon_feedpin.size() != 0) {
						if (W_icon_feedpin.get(0).ownText().contains("置顶")) {
							continue;
						}
					}
				}
				logger.info("no more weibo published on yesterday.");
				haveMore = false;
				break;
			}
			
			String likeUrl = URL_TEMPLATE_LIKE.replace("{mid}",
					linkEle.attr("name"))
					+ System.currentTimeMillis();
			Request request = new Request(likeUrl);
			request.putExtra(REQUEST_EXTRA_TYPE, REQUEST_TYPE_TARGET);
			request.putExtra(REQUEST_EXTRA_TASK,
					currentRequest.getExtra(REQUEST_EXTRA_TASK));
			String content = contentEles.get(i).text();
			if (content.length() > 20) {
				content = content.substring(0, 20);
			}
			request.putExtra(REQUEST_EXTRA_CONTENT, content);
			page.addTargetRequest(request);
			logger.info("push target - {}", likeUrl);
		}

		if (haveMore) {
			String naviUrl = null;
			int pagebar = (Integer) currentRequest
					.getExtra(REQUEST_EXTRA_PAGEBAR);
			int pagenum = (Integer) currentRequest.getExtra(REQUEST_EXTRA_PAGE);
			String task = (String) currentRequest.getExtra(REQUEST_EXTRA_TASK);
			String data_queryfix = (String) currentRequest
					.getExtra(REQUEST_EXTRA_DATA_QUERYFIX);
			String page_id = (String) currentRequest
					.getExtra(REQUEST_EXTRA_PAGE_ID);

			int requestType;
			if (pagebar < 1) {
				requestType = REQUEST_TYPE_FALLS;
				pagebar++;
				if (pagenum == 1) {
					naviUrl = "http://weibo.com/p/aj/v6/mblog/mbloglist?ajwvr=6&domain=100606&"
							+ data_queryfix
							+ "&pagebar="
							+ pagebar
							+ "&pl_name=Pl_Official_MyProfileFeed__25&id="
							+ page_id
							+ "&script_uri=/"
							+ task
							+ "&feed_type=0&page=1&pre_page=1&domain_op=100606&__rnd="
							+ System.currentTimeMillis();
				} else {
					naviUrl = "http://weibo.com/p/aj/v6/mblog/mbloglist?ajwvr=6&domain=100606&is_search=0&visible=0&"
							+ data_queryfix
							+ "&is_tag=0&profile_ftype=1&page="
							+ pagenum
							+ "&pagebar="
							+ pagebar
							+ "&pl_name=Pl_Official_MyProfileFeed__25&id="
							+ page_id
							+ "&script_uri=/"
							+ task
							+ "&feed_type=0&pre_page="
							+ pagenum
							+ "&domain_op=100606&__rnd="
							+ System.currentTimeMillis();
				}
			} else {
				requestType = REQUEST_TYPE_NAVI;
				pagebar = -1;
				pagenum++;
				naviUrl = "http://weibo.com/"
						+ task
						+ "?pids=Pl_Official_MyProfileFeed__25&is_search=0&visible=0&"
						+ data_queryfix + "&is_tag=0&profile_ftype=1&page="
						+ pagenum + "#feedtop";
			}

			Request nextRequest = new Request(naviUrl);
			nextRequest.putExtra(REQUEST_EXTRA_TASK, task);
			nextRequest.putExtra(REQUEST_EXTRA_TYPE, requestType);
			nextRequest.putExtra(REQUEST_EXTRA_PAGEBAR, pagebar);
			nextRequest.putExtra(REQUEST_EXTRA_PAGE, pagenum);
			nextRequest.putExtra(REQUEST_EXTRA_DATA_QUERYFIX, data_queryfix);
			nextRequest.putExtra(REQUEST_EXTRA_PAGE_ID, page_id);
			page.addTargetRequest(nextRequest);
			logger.info("push navi - {}", naviUrl);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		File tasksFile = new File(ConstantsHome.USER_DIR + File.separator
				+ "tasks");
		if (!tasksFile.isFile()) {
			System.err.println("没有找到任务文件 - " + tasksFile.getAbsolutePath());
			return;
		}
		Set<String> tasks;
		try {
			tasks = new HashSet(FileUtils.readLines(tasksFile));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		if (null == tasks || tasks.size() == 0) {
			System.err.println("没有找到任务");
			return;
		}

		File cookieFile = new File(ConstantsHome.USER_DIR + File.separator
				+ "cookie");
		if (!cookieFile.isFile()) {
			System.err
					.println("没有找到cookie文件 - " + cookieFile.getAbsolutePath());
			return;
		}
		String cookie;
		try {
			cookie = FileUtils.readFileToString(cookieFile);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		if (null == cookie || cookie.length() == 0) {
			System.err.println("没有找到cookie - " + cookieFile.getAbsolutePath());
			return;
		}

		// 获取配置
		MySite site;
		try {
			site = MySiteBuilder.buildMySite();
		} catch (ParserConfigurationException | SAXException | IOException
				| ConfigurationException e) {
			logger.error("spider config load error", e);
			return;
		}
		site.addHeader("Accept", "text/html, application/xhtml+xml, */*")
				.addHeader("Accept-Encoding", "gzip, deflate")
				.addHeader("Accept-Language", "zh-Hans-CN,zh-Hans;q=0.5")
				.addHeader("Connection", "Keep-Alive")
				.addHeader("Cache-Control", "no-cache")
				.addHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");
		site.addHeader("Cookie", cookie);

		// 创建爬虫
		BasicPageProcessor pageProcessor = new WeiboLikeInfoPageProcessor(site);
		List<SpiderListener> spiderListeners = new ArrayList<SpiderListener>(1);
		spiderListeners.add(pageProcessor);
		Spider spider = MySpider.create(pageProcessor);
		spider.setSpiderListeners(spiderListeners)
				.addPipeline(new MyNullPipeline())
				.setScheduler(new QueueScheduler())
				.setDownloader(new MyHttpClientDownloader());

		// 添加任务
		for (String task : tasks) {
			Request request = new Request(URL_PREFIX + task + URL_SUFFIX);
			request.putExtra(REQUEST_EXTRA_TASK, task);
			request.putExtra(REQUEST_EXTRA_TYPE, REQUEST_TYPE_START);
			request.putExtra(REQUEST_EXTRA_PAGEBAR, -1);
			request.putExtra(REQUEST_EXTRA_PAGE, 1);
			spider.addRequest(request);
			logger.info("push start - {}", request.getUrl());
		}

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
