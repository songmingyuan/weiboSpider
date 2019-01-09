package com.cmcc.wltx.collector.pageprocessor.weibo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.MyNullPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.exception.PageProcessException;

/**
 * @author mingyuan.song 微博根据关键词进行搜索（综合）
 *
 */
public class WeiboKeyWordSearchPageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(WeiboKeyWordSearchPageProcessor.class);
	private static final String SEARCH_URL_PREFIX = "http://s.weibo.com/weibo/";
	private static final String SEARCH_URL_SUFFIX = "&Refer=STopic_box";

	public WeiboKeyWordSearchPageProcessor(MySite site) {
		super(site);
	}

	@Override
	public void process(Page page) throws PageProcessException {
		String rawText = page.getRawText();
		int index_of_pl_weibo_feedList_start = rawText
				.indexOf("<script>STK && STK.pageletM && STK.pageletM.view({\"pid\":\"pl_weibo_direct\",");
		if (-1 == index_of_pl_weibo_feedList_start) {
			throw new PageProcessException("-1 == index_of_pl_weibo_feedList_start");
		}
		index_of_pl_weibo_feedList_start += 49;
		int index_of_pl_weibo_feedList_end = rawText.indexOf(")</script>", index_of_pl_weibo_feedList_start);
		if (-1 == index_of_pl_weibo_feedList_end) {
			throw new PageProcessException("-1 == index_of_pl_weibo_feedList_end");
		}
		String html;
		try {
			JSObject pl_weibo_feedList = JSUtils.createJSObject(
					rawText.substring(index_of_pl_weibo_feedList_start, index_of_pl_weibo_feedList_end));
			html = pl_weibo_feedList.getNotNullString("html").trim();
		} catch (JSException e) {
			throw new PageProcessException("JSON结构异常", e);
		}
		Document doc = Jsoup.parse(html);
		Elements feedListEles = doc.select("div.WB_cardwrap.S_bg2.clearfix > div[tbinfo]");
		if (feedListEles.isEmpty()) {
			logger.warn("feedListEles.isEmpty()");
			return;
		}
		for (Element feedListEle : feedListEles) {
			String uid = feedListEle.attr("tbinfo").trim();
			if (uid.isEmpty()) {
				logger.warn("uid.isEmpty()");
				continue;
			}
			System.out.println(uid);
		}
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

	@SuppressWarnings("unchecked")
	private static void launch(String[] spiderIds) {
		MySpider.test = spiderIds[spiderIds.length - 1].startsWith(MySpider.PREFIX_ID_TEST);
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
		BasicPageProcessor pageProcessor = new WeiboKeyWordSearchPageProcessor(site);
		int proxyType = MySpider.test ? 0 : 1;
		MySpider spider = MySpider.create(pageProcessor, null, new QueueScheduler(), proxyType, new MyNullPipeline());

		// TODO后续会变成查数据表
		// 获取搜索关键词
		File keywordsFile = new File(ConstantsHome.USER_DIR + File.separatorChar + "keywords");
		if (!keywordsFile.isFile()) {
			logger.error("缺少关键词文件");
			return;
		}

		List<String> keywords;
		try {
			keywords = FileUtils.readLines(keywordsFile);
		} catch (IOException e) {
			logger.error("关键词读取失败", e);
			return;
		}
		keywordsFile.delete();

		if (keywords.isEmpty()) {
			logger.error("关键词文件是空的");
			return;
		}

		// 添加任务启动爬虫
		for (String keyword : keywords) {
			try {
				keyword = URLEncoder.encode(URLEncoder.encode(keyword, ConstantsHome.CHARSET_DEFAULT),
						ConstantsHome.CHARSET_DEFAULT);
			} catch (UnsupportedEncodingException e) {
				logger.error("不支持编码 - " + ConstantsHome.CHARSET_DEFAULT, e);
				return;
			}
			spider.addRequest(new Request(SEARCH_URL_PREFIX + keyword + SEARCH_URL_SUFFIX));
		}

		try {
			spider.run();
		} finally {
			if (null != spider) {
				spider.close();
			}
		}
	}
}
