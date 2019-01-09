package com.cmcc.wltx.collector.pageprocessor.weibo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.exception.PageProcessException;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.json.JSException;
import com.cmcc.json.JSObject;
import com.cmcc.json.JSUtils;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.constraint.RedisBasedUniqueConstraint;
import com.cmcc.wltx.collector.constraint.RedisClusterBasedUniqueConstraint;
import com.cmcc.wltx.collector.exception.LogicError;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.FollowedWeiboSpiderTask;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.scheduler.QueueScheduler;
import com.cmcc.wltx.collector.service.FollowedWeiboSpiderTaskService;
import com.cmcc.wltx.collector.service.ServiceFactory;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.spider.model.Article;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.ArticlesOutFileWriterPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.MyNullPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;
import com.cmcc.wltx.common.VerticalOutFileWriter;
import com.cmcc.wltx.database.JedisUtils;
import com.cmcc.wltx.utils.WeiBoUtils;

public class FollowedWeiboPageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FollowedWeiboPageProcessor.class);
	private static final int REQUEST_TYPE_HOME = 0;
	private static final int REQUEST_TYPE_FALLS = 1;
	private static final String REQUEST_EXTRA_WEIBO_FOLLOW_TYPE = "weibo_follow_type";
	private static final String REQUEST_EXTRA_PAGEBAR = "pagebar";
	private static final String REQUEST_EXTRA_PAGE_NUM = "page_num";
	private static final String REQUEST_EXTRA_END_ID = "end_id";
	private final String REDIS_KEY_PREFIX_MID = "MID_";

	public FollowedWeiboPageProcessor(MySite site) {
		super(site);
	}

	@Override
	public void process(Page page) throws PageProcessException {
		Request currentRequest = page.getRequest();

		String rawText = page.getRawText();
		int indexOf = rawText.indexOf("<title>Sina Visitor System</title>");
		if (-1 == indexOf) {
			indexOf = rawText.indexOf("<title>新浪通行证</title>");
		}
		if (-1 != indexOf) {
			@SuppressWarnings("unchecked")
			Map<String, String> header = (Map<String, String>) currentRequest
					.getExtra(ConstantsHome.REQUEST_EXTRA_HEADERS);
			logger.warn(String.valueOf(indexOf) + " - invalid cookie - {}",
					header.get(ConstantsHome.REQUEST_HEADER_COOKIE));
			FollowedWeiboSpiderTaskService taskService = ServiceFactory.getFollowedWeiboSpiderTaskService();
			try {
				taskService.removeInvalidCookie((Long) currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_TASK_ID));
			} catch (ServiceException e) {
				throw new PageProcessException(e);
			}
			return;
		}

		Integer type = (Integer) currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_TYPE);
		Integer weiboFollowType = (Integer) currentRequest.getExtra(REQUEST_EXTRA_WEIBO_FOLLOW_TYPE);
		boolean isFocus = (1 == weiboFollowType);
		switch (type) {
		case REQUEST_TYPE_HOME:
			processHomePage(page, isFocus);
			break;
		case REQUEST_TYPE_FALLS:
			processFallsPage(page, isFocus);
			break;
		default:
			throw new LogicError("错误的请求类型 - " + type);
		}
	}

	private void processFallsPage(Page page, boolean isFocus) throws PageProcessException {
		try {
			JSObject jsPage = JSUtils.createJSObject(page.getRawText());
			Document doc = Jsoup.parse(jsPage.getNotNullString("data"), page.getRequest().getUrl());
			Elements homefeed = doc.select(":root > body > div[node-type=homefeed]");
			if (homefeed.size() != 1) {
				throw new PageProcessException("homefeed.size() = " + homefeed.size());
			}

			processWithFocus(isFocus, page, homefeed.get(0));
		} catch (JSException e) {
			throw new PageProcessException("返回JSON结果异常", e);
		}
	}

	private void processHomePage(Page page, boolean isFocus) throws PageProcessException {
		String rawText = page.getRawText();
		int indexFMViewScript = rawText.indexOf("<script>FM.view({\"ns\":\"pl.content.homefeed.index\"");
		if (indexFMViewScript == -1) {
			throw new PageProcessException("indexFMViewScript == -1");
		}
		int beginIndex = indexFMViewScript + 16;
		int endIndex = rawText.indexOf(")</script>", indexFMViewScript);
		if (endIndex < 0) {
			throw new PageProcessException("endIndex = " + endIndex);
		}
		JSObject jp = JSUtils.createJSObject(rawText.substring(beginIndex, endIndex));
		Document doc = Jsoup.parse(jp.getNotNullString("html"), page.getRequest().getUrl());
		Elements feed_list = doc.select(":root > body > div[node-type=homefeed] > div[node-type=feed_list]");
		if (feed_list.size() != 1) {
			throw new PageProcessException("feed_list.size() = " + feed_list.size());
		}
		processWithFocus(isFocus, page, feed_list.get(0));
	}

	private void processWithFocus(boolean isFocus, Page page, Element feedList) throws PageProcessException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			if (isFocus) {
				conn = MyDataSource.connect();
				stmt = conn.prepareStatement("insert into yq_focus_weibo values(?,?,?,?,?,?,?);");
			}
			processFeeds(page, feedList, stmt);
		} catch (SQLException e) {
			throw new PageProcessException("insert into yq_focus_weibo failed", e);
		} finally {
			if (isFocus) {
				MyDataSource.release(stmt, conn);
			}
		}
	}

	private void processFeeds(Page page, Element feedListEle, PreparedStatement stmt)
			throws PageProcessException, SQLException {
		Elements feedEles = feedListEle.children();
		if (feedEles.isEmpty()) {
			throw new PageProcessException("feedEles.isEmpty()");
		}

		boolean noMore = false;
		List<Article> articles = new ArrayList<Article>();
		long now = System.currentTimeMillis();
		Request currentRequest = page.getRequest();
		String endId = (String) currentRequest.getExtra(REQUEST_EXTRA_END_ID);
		String minId = null;
		for (Element feedEle : feedEles) {
			String feedType = feedEle.attr("feedtype").trim();
			if (!feedType.isEmpty()) {
				if ("ad".equals(feedType)) { // 跳过广告
					continue;
				}
				logger.warn("feedType = {}", feedType);
			}
			String mid = feedEle.attr("mid").trim();
			if (mid.length() == 0) {
				continue;
			}
			minId = mid;
			if (null == endId) {
				endId = mid;
			}

			// 去重
			if (!getMySpider().isUnique(REDIS_KEY_PREFIX_MID + mid)) {
				noMore = true;
				logger.info("duplicate - {}", mid);
				break;
			}

			// 时间
			Elements dateEles = feedEle
					.select(":root > div[node-type=feed_content] > div.WB_detail > div.WB_from > a[date~=\\d{13,}]");
			if (dateEles.size() != 1) {
				throw new PageProcessException("dateEles.size()=" + dateEles.size() + " - " + mid);
			}
			long pubtime = Long.parseLong(dateEles.get(0).attr("date").trim());
			if (now - pubtime > 86400000) { // 超过1天过期
				noMore = true;
				logger.info("expired - {}", pubtime);
				break;
			}

			// url
			Elements user = feedEle.select(
					":root > div[node-type=feed_content] > div.WB_detail > div.WB_info > a[usercard~=id=\\d+&.*]");
			if (user.size() != 1) {
				throw new PageProcessException("user.size()=" + user.size() + " - " + mid);
			}
			String usercard = user.get(0).attr("usercard");
			String uid = usercard.substring(3, usercard.indexOf('&'));
			String url = WeiBoUtils.getReference(uid, mid);
			Article article = createArticle(url, 2);
			article.setTitle("新浪微博");
			article.setCreateDate(new Date(pubtime));

			// 作者
			String nickName = user.get(0).ownText();
			article.setAuthor(nickName);

			// 正文
			Elements contentEles = feedEle.select(":root > div[node-type=feed_content] > div.WB_detail > div.WB_text");
			if (contentEles.size() != 1) {
				throw new PageProcessException("contentEles.size()=" + contentEles.size() + " - " + mid);
			}
			article.setContent(contentEles.get(0).text());

			articles.add(article);
			if (null != stmt) {
				stmt.setString(1, UUID.randomUUID().toString());
				stmt.setString(2, uid);
				stmt.setString(3, nickName);
				stmt.setString(4, contentEles.get(0).text());
				stmt.setTimestamp(5, new Timestamp(pubtime));
				stmt.setTimestamp(6, new Timestamp(now));
				stmt.setString(7, url);
				stmt.addBatch();
			}
		}

		if (!articles.isEmpty()) {
			page.setSkip(false);
			page.putField(ArticlesOutFileWriterPipeline.KEY_ARTICLES, articles);
			if (null != stmt) {
				stmt.executeBatch();
			}
		}

		if (noMore) {
			return;
		}

		String naviUrl = null;
		int requestType;
		int pagebar = (Integer) currentRequest.getExtra(REQUEST_EXTRA_PAGEBAR);
		int pagenum = (Integer) currentRequest.getExtra(REQUEST_EXTRA_PAGE_NUM);
		if (pagebar < 1) {
			requestType = REQUEST_TYPE_FALLS;
			pagebar++;
			naviUrl = "http://weibo.com/aj/mblog/fsearch?ajwvr=6&pre_page=" + pagenum + "&page=" + pagenum + "&end_id="
					+ endId + "&min_id=" + minId + "&is_ori=1&pids=Pl_Content_HomeFeed&pagebar=" + pagebar + "&__rnd="
					+ System.currentTimeMillis();

		} else {
			if (pagenum >= 10) {
				return;
			}
			pagenum++;
			requestType = REQUEST_TYPE_HOME;
			pagebar = -1;
			Elements nextPageEles = feedListEle
					.select(":root > div.WB_cardwrap > div.W_pages > a[action-type=feed_list_page]");
			if (nextPageEles.size() != 1) {
				throw new PageProcessException("nextPageEles.size() != 1");
			}
			Element nextPageEle = nextPageEles.get(0);
			if (!nextPageEle.ownText().contains("下一页")) {
				throw new PageProcessException("没有找到下一页");
			}
			naviUrl = nextPageEle.absUrl("href");
		}

		Request nextRequest = new Request(naviUrl);
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_HEADERS,
				currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_HEADERS));
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY,
				currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY));
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_TASK_ID,
				currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_TASK_ID));
		nextRequest.putExtra(REQUEST_EXTRA_WEIBO_FOLLOW_TYPE, currentRequest.getExtra(REQUEST_EXTRA_WEIBO_FOLLOW_TYPE));
		nextRequest.putExtra(REQUEST_EXTRA_END_ID, endId);
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, requestType);
		nextRequest.putExtra(REQUEST_EXTRA_PAGEBAR, pagebar);
		nextRequest.putExtra(REQUEST_EXTRA_PAGE_NUM, pagenum);
		page.addTargetRequest(nextRequest);
		logger.info("push navi - {}", naviUrl);
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
		String spiderId = spiderIds[spiderIds.length - 1];
		MySpider.test = spiderId.startsWith(MySpider.PREFIX_ID_TEST);

		// 获取配置
		SpiderService spiderService = ServiceFactory.getSpiderService();
		MySite site;
		try {
			site = spiderService.buildMySite(spiderIds);
			site.setStoreCookie(false);
		} catch (ServiceException e) {
			logger.error("spider config load error", e);
			return;
		}

		// 初始化redis
		String redisHost = site.getRedisHost();
		int redisPort = site.getRedisPort();
		if (null == redisHost || redisHost.isEmpty() || redisPort <= 0) {
			logger.error("redis config error");
			return;
		}
		JedisUtils.initPool(redisHost, redisPort);
		FollowedWeiboSpiderTaskService taskService = null;
		MySpider spider = null;
		try {
			// 创建爬虫
			taskService = ServiceFactory.getFollowedWeiboSpiderTaskService();
			spider = MySpider.create(new FollowedWeiboPageProcessor(site), null, new QueueScheduler(),
					MySpider.test ? 0 : 1, MySpider.test ? new MyNullPipeline()
							: new ArticlesOutFileWriterPipeline(site.getFilePipelinePath(), false, 100));
			if (MySpider.test) {
				spider.setUniqueConstraint(new RedisBasedUniqueConstraint(90000));
			} else {
				spider.setUniqueConstraint(new RedisClusterBasedUniqueConstraint(90000));
			}
			do {
				// 先释放任务（如果有异常退出的情况可能存在之前未释放掉的任务）
				try {
					taskService.releaseTasks(spiderId);
				} catch (ServiceException e) {
					logger.error("release tasks failed", e);
					break;
				}

				// 获取任务
				List<FollowedWeiboSpiderTask> tasksForCrawl;
				try {
					tasksForCrawl = taskService.tasksForCrawl(spiderId, site.getTaskLimit());
				} catch (ServiceException e) {
					logger.error("get tasks for crawl failed", e);
					return;
				}
				if (0 != tasksForCrawl.size()) {
					try (Jedis jedis = JedisUtils.createJedis()) {
						for (FollowedWeiboSpiderTask task : tasksForCrawl) {
							Request request = new Request("http://weibo.com/u/" + task.getId() + "/home?is_ori=1#_0");
							Map<String, String> requestHeaders = new HashMap<String, String>();
							requestHeaders.put(ConstantsHome.REQUEST_HEADER_COOKIE, task.getCookie());
							request.putExtra(ConstantsHome.REQUEST_EXTRA_HEADERS, requestHeaders);

							request.putExtra(ConstantsHome.REQUEST_EXTRA_TASK_ID, task.getId());
							request.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, REQUEST_TYPE_HOME);
							request.putExtra(REQUEST_EXTRA_WEIBO_FOLLOW_TYPE, task.getType());
							request.putExtra(REQUEST_EXTRA_PAGEBAR, -1);
							request.putExtra(REQUEST_EXTRA_PAGE_NUM, 1);
							spider.addRequest(request);
							logger.info("start task - {}", task.getId());
						}
					}

					spider.run();
					try {
						VerticalOutFileWriter.closeAll();
					} catch (IOException e) {
						logger.error("tmp转out文件失败", e);
						break;
					}
				} else {
					logger.info("no task for crawl");
					if (!ConstantsHome.BLOCK.exists()) {
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							logger.info("no task sleep interrupted", e);
						}
					}
				}

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
			} while (true);
		} finally {
			try {
				VerticalOutFileWriter.closeAll();
			} catch (IOException e) {
				logger.error("tmp转out文件失败", e);
			} finally {
				try {
					if (null != taskService) {
						taskService.releaseTasks(spiderId);
					}
				} catch (ServiceException e) {
					logger.error("release tasks failed", e);
				} finally {
					try {
						if (null != spider) {
							spider.close();
						}
					} finally {
						JedisUtils.closePool();
					}
				}
			}
		}
	}
}
