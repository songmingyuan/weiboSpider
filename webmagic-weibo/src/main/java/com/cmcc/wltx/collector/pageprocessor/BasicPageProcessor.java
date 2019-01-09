package com.cmcc.wltx.collector.pageprocessor;

import java.util.Date;
import java.util.Map;

import org.apache.http.client.utils.DateUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.processor.PageProcessor;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.spider.model.Article;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;

public abstract class BasicPageProcessor implements PageProcessor,
		SpiderListener {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BasicPageProcessor.class);
	protected static final org.slf4j.Logger articlesLogger = org.slf4j.LoggerFactory
			.getLogger("pageprocessor.ArticlesLogger");
	
	public static final int PROCESS_FAILED = 0;
	public static final int PROCESS_SUCCESS = 1;
	public static final int PROCESS_UNFINISHED = 2;

	private MySpider mySpider;
	private MySite mySite;

	public static final String SUFFIX_REQUEST_ERROR = "request failed";
	protected static final String PREFIX_HTTP = "http://";


	public BasicPageProcessor(MySite mySite) {
		super();
		this.mySite = mySite;
	}

	@Override
	public void onSuccess(Request request) {}

	@Override
	public void onError(Request request, Throwable e) {
		String url = request.getUrl();
		logger.warn("process request failed - {}", url);
	}

	@Override
	public void onAddRequestException(int pushResult, Request request) {
	}
	
	@Override
	public void onExitWhenComplete() {
		logger.info("process all finished.");
	}
	@Override
	public void onWaitWhenComplete() {
		logger.info("process on waiting...");
	}

	@Override
	public Site getSite() {
		return mySite;
	}

	public MySite getMySite() {
		return mySite;
	}

	/**
	 * 创建文章对象
	 * @param url
	 * @return
	 */
	protected Article createArticle(String url, int recType) {
		if (recType <= 0) {
			recType = getMySite().getRecType();
		}
		Article article = new Article(recType);
		article.setUrl(url);
		article.setSiteName(this.getMySite().getName());
		return article;
	}

	protected void addTopicInfo(Article topicArticle, Article article) {
		article.setTopicTitle(topicArticle.getListTitle());
		article.setTopicDesc(topicArticle.getSummary());
		article.setTopicAuthor(topicArticle.getAuthor());
		article.setTopicCreateDate(topicArticle.getCreateDate());
		article.setTopicEditDate(topicArticle.getEditDate());
	}
	
	protected int pushRequest(Request request) {
		return this.mySpider.getScheduler().push(request, this.mySpider);
	}

	protected MySpider getMySpider() {
		return mySpider;
	}

	public void setMySpider(MySpider mySpider) {
		this.mySpider = mySpider;
	}
	
	protected Date getLastModified(Page page) {
		Map<String, String> headers = page.getHeaders();
		String string = headers.get(ConstantsHome.REQUEST_HEADER_LASTMODIFIED);
		if (null != string && string.length() != 0) {
			return DateUtils.parseDate(string);
		}
		return null;
	}

}
