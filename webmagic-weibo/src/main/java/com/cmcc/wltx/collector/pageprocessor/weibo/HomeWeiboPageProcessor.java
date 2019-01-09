package com.cmcc.wltx.collector.pageprocessor.weibo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.exception.PageProcessException;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.json.JSException;
import com.cmcc.json.JSObject;
import com.cmcc.json.JSUtils;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.exception.PageStructureException;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.HomeWeiboSpiderTask;
import com.cmcc.wltx.collector.model.WeiboUser;
import com.cmcc.wltx.collector.pageprocessor.BasicPageProcessor;
import com.cmcc.wltx.collector.scheduler.QueueScheduler;
import com.cmcc.wltx.collector.service.HomeWeiboSpiderTaskService;
import com.cmcc.wltx.collector.service.ServiceFactory;
import com.cmcc.wltx.collector.service.SpiderService;
import com.cmcc.wltx.collector.service.WeiboUserService;
import com.cmcc.wltx.collector.service.WeiboVisitorCookieService;
import com.cmcc.wltx.collector.spider.model.Article;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.collector.spider.mywebmagic.downloader.MyHttpClientDownloader;
import com.cmcc.wltx.collector.spider.mywebmagic.pipeline.ArticlesOutFileWriterPipeline;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;
import com.cmcc.wltx.common.VerticalOutFileWriter;
import com.cmcc.wltx.database.JedisUtils;
import com.cmcc.wltx.utils.WeiBoUtils;


public class HomeWeiboPageProcessor extends BasicPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HomeWeiboPageProcessor.class);
	private final HomeWeiboSpiderTaskService taskService = ServiceFactory.getHomeWeiboSpiderTaskService();
	private final WeiboUserService weiboUserService = ServiceFactory.getWeiboUserService();

	private static final int REQUEST_TYPE_START = 0;
	private static final int REQUEST_TYPE_FALLS = 1;
	private static final int REQUEST_TYPE_NAVI = 2;

	private static final String REQUEST_EXTRA_TASK_LEVEL = "taskLevel";
	private static final String REQUEST_EXTRA_WEIBO_USER = "weiboUser";
	private static final String REQUEST_EXTRA_PAGEBAR = "pagebar";
	private static final String REQUEST_EXTRA_PAGE_NUM = "page_num";
	private static final String REQUEST_EXTRA_PAGE_ID = "page_id";
	private static final String REQUEST_EXTRA_DOMAIN = "domain_op";
	private static final String REQUEST_EXTRA_PL_NAME = "pl_name";
	// private static final String REQUEST_EXTRA_NICKNAME = "onick";

	private final String REDIS_KEY_PREFIX_MID = "mid_";

	private final String REDIS_KEY_PREFIX_IMGID = "imgId_";

	private final Pattern PA_PAGE_ID = Pattern.compile("\\$CONFIG\\['page_id'\\]='([^']+)';");
	private final Pattern PA_ONICK = Pattern.compile("\\$CONFIG\\['onick'\\]='([^']+)';");
	private final Pattern PA_DOMAIN = Pattern.compile("\\$CONFIG\\['domain'\\]='([^']+)';");
	private final Pattern PA_FOLLOW_BRACKET = Pattern.compile("\\\\/follow\\\\\"\\s*>.的关注\\((\\d+)\\)<");
	private final Pattern PA_FOLLOW = Pattern.compile(
			"<strong class=\\\\\"W_f\\d+\\\\\">(\\d+)<\\\\/strong><span class=\\\\\"S_txt2\\\\\">关注<\\\\/span>");
	private final Pattern PA_FAN_BRACKET = Pattern.compile("\\?relate=fans\\\\\"\\s*>.的粉丝\\((\\d+)\\)<");
	private final Pattern PA_FAN = Pattern.compile(
			"<strong class=\\\\\"W_f\\d+\\\\\">(\\d+)<\\\\/strong><span class=\\\\\"S_txt2\\\\\">粉丝<\\\\/span>");
	private final Pattern PA_COUNT_FEED = Pattern.compile(
			"<strong class=\\\\\"W_f\\d+\\\\\">(\\d+)<\\\\/strong><span class=\\\\\"S_txt2\\\\\">微博<\\\\/span>");
	private final int EXPIRE = 172800;
	private final int EXPIRE_M = EXPIRE * 1000;

	public HomeWeiboPageProcessor(MySite site) {
		super(site);
	}

	@Override
	public void process(Page page) throws PageProcessException {
		Request currentRequest = page.getRequest();
		int type = (Integer) currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_TYPE);
		try {
			switch (type) {
			case REQUEST_TYPE_START:
				processNaviPage(page, true);
				break;
			case REQUEST_TYPE_NAVI:
				processNaviPage(page, false);
				break;
			case REQUEST_TYPE_FALLS:
				processFallsPage(page);
				break;
			default:
				break;
			}
		} catch (PageStructureException e) {
			// 页面结构异常很可能是由于使用外网代理造成的，所以如果用的是外网代理，换代理重新访问，直到换到内网代理还是异常的话就过掉
			HttpHost proxy = (HttpHost) currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY);
			if (null != proxy && !proxy.getHostName().startsWith("172.17.18.")) {
				logger.warn(e.getMessage());
				currentRequest.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY, null);
				currentRequest.putExtra(Request.CYCLE_TRIED_TIMES, null);
				currentRequest.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE, null);
				page.addTargetRequest(currentRequest);
			} else {
				throw new PageProcessException(e);
			}
		}
	}

	private void processFallsPage(Page page) throws PageProcessException, PageStructureException {
		// 获取feedList
		String rawText = page.getRawText();
		Document doc;
		try {
			JSObject jp = JSUtils.createJSObject(rawText);
			String html = jp.getString("data");
			doc = Jsoup.parse(html);
		} catch (JSException e) {
			throw new PageStructureException("FallsPage解析JSON异常", e);
		}
		Element feedList;
		try {
			feedList = doc.child(0).child(1);
		} catch (IndexOutOfBoundsException e) {
			throw new PageStructureException("FallsPage获取feedList失败", e);
		}
		processFeedList(page, feedList, false);
	}

	private void processNaviPage(Page page, boolean isFirstPage) throws PageProcessException, PageStructureException {
		Request currentRequest = page.getRequest();
		WeiboUser user = (WeiboUser) currentRequest.getExtra(REQUEST_EXTRA_WEIBO_USER);
		int taskLevel = (Integer) currentRequest.getExtra(REQUEST_EXTRA_TASK_LEVEL);
		// 任务级别小于0的是基础信息补采任务
		boolean isBaseTask = taskLevel < 0;
		String rawText = page.getRawText();

		// 跳转到首页表示该微博账号可能不存在
		if (isFirstPage) {
			if (-1 != rawText.indexOf("<title>微博-随时随地发现新鲜事</title>")) {
				logger.info("task error not exist - {} @Lv {}", user.getId(), taskLevel);
				if (taskLevel == HomeWeiboSpiderTask.LEVEL_TEMP) {
					try {
						weiboUserService.updateStatusById(WeiboUser.STATUS_NOTEXIST, user.getId());
					} catch (ServiceException e) {
						logger.warn("update status by id failed - " + user.getId(), e);
					}
				} else {
					// TODO 已经开始采集的微博也可能被删，需要处理
				}
				return;
			}
		}

		if (-1 != rawText.indexOf("<title>404错误</title>")) {
			logger.info("task error 404 - {} @Lv {}", user.getId(), taskLevel);
			if (isBaseTask) {
				return;
			}
			if (isFirstPage) {
				if (taskLevel == HomeWeiboSpiderTask.LEVEL_TEMP) {
					try {
						user.setNickName("#404");
						user.setStatus(WeiboUser.STATUS_404);
						user.setFanCount(0l);
						user.setFollowCount(0l);
						weiboUserService.update(user, null);
					} catch (ServiceException e) {
						logger.warn("update failed - " + user.getId(), e);
					}
				} else if (taskLevel != HomeWeiboSpiderTask.LEVEL_404) {
					taskService.transferLevel(user, taskLevel, HomeWeiboSpiderTask.LEVEL_404);
				}
			}
			return;
		}

		if (isFirstPage) {
			// 准备好一些导航需要的参数
			Matcher ma = PA_PAGE_ID.matcher(rawText);
			if (!ma.find()) {
				throw new PageStructureException("没有找到page_id");
			}
			currentRequest.putExtra(REQUEST_EXTRA_PAGE_ID, ma.group(1));

			// 昵称
			ma.usePattern(PA_ONICK);
			if (!ma.find()) {
				throw new PageStructureException("没有找到onick");
			}
			String onick = ma.group(1);
			user.setNickName(onick);

			// 导航需要的参数
			ma.usePattern(PA_DOMAIN);
			if (!ma.find()) {
				throw new PageStructureException("没有找到domain");
			}
			currentRequest.putExtra(REQUEST_EXTRA_DOMAIN, ma.group(1));

			if (taskLevel == HomeWeiboSpiderTask.LEVEL_TEMP || isBaseTask) {
				// 关注数
				ma.usePattern(PA_FOLLOW);
				if (!ma.find()) {
					ma = PA_FOLLOW_BRACKET.matcher(rawText);
					if (!ma.find()) {
						throw new PageStructureException("没有找到关注数");
					}
				}
				user.setFollowCount(Long.parseLong(ma.group(1)));
			}

			// 粉丝数
			ma.usePattern(PA_FAN);
			if (!ma.find()) {
				ma = PA_FAN_BRACKET.matcher(rawText);
				if (!ma.find()) {
					throw new PageStructureException("没有找到粉丝数");
				}
			}
			user.setFanCount(Long.parseLong(ma.group(1)));

			if (taskLevel == HomeWeiboSpiderTask.LEVEL_TEMP || isBaseTask) {
				// 微博数
				ma.usePattern(PA_COUNT_FEED);
				if (!ma.find()) {
					throw new PageStructureException("没有找到微博数");
				}
				user.setFeedCount(Long.parseLong(ma.group(1)));

				// 头像链接
				int indexOfHeaderHead = rawText.indexOf(
						"<script>FM.view({\"ns\":\"pl.header.head.index\",\"domid\":\"Pl_Official_Headerv6__1\"");
				if (indexOfHeaderHead == -1) {
					indexOfHeaderHead = rawText.indexOf("<script>FM.view({\"ns\":\"pl.header.preloginHead.index\"");
					if (indexOfHeaderHead == -1) {
						throw new PageProcessException("indexOfHeaderHead == -1");
					}
				}
				int indexOfHeaderFoot = rawText.indexOf(")</script>", indexOfHeaderHead);
				if (-1 == indexOfHeaderFoot) {
					throw new PageStructureException("-1 == indexOfHeaderFoot");
				}
				String headerHtml;
				try {
					JSObject jso = JSUtils.createJSObject(rawText.substring(indexOfHeaderHead + 16, indexOfHeaderFoot));
					headerHtml = jso.getNotNullString("html");
				} catch (JSException e) {
					throw new PageStructureException("pl.header.head.index解析JSON异常", e);
				}
				Document headerDoc = Jsoup.parse(headerHtml);
				Elements avatarEles = headerDoc.select("div.pf_photo > p.photo_wrap > img.photo");
				if (1 != avatarEles.size()) {
					throw new PageStructureException("1 != avatarEles.size()");
				}
				String avatarUrl = avatarEles.get(0).attr("src").trim();
				if (avatarUrl.isEmpty()) {
					throw new PageStructureException("avatarUrl.isEmpty()");
				}
				user.setAvatarUrl(avatarUrl);

				// 认证类型
				Elements iconEles = headerDoc.select("div.pf_photo > a.icon_bed > em");
				if (0 == iconEles.size()) {
					user.setVerifyType(WeiboUser.TYPE_VERIFY_NONE);
				} else if (1 == iconEles.size()) {
					Set<String> classNames = iconEles.get(0).classNames();
					if (classNames.contains("icon_pf_approve_gold")) {
						user.setVerifyType(WeiboUser.TYPE_VERIFY_GOLD);
					} else if (classNames.contains("icon_pf_approve")) {
						user.setVerifyType(WeiboUser.TYPE_VERIFY_YELLOW);
					} else if (classNames.contains("icon_pf_approve_co")) {
						user.setVerifyType(WeiboUser.TYPE_VERIFY_BLUE);
					} else if (classNames.contains("icon_pf_club")) {
						user.setVerifyType(WeiboUser.TYPE_VERIFY_CLUB);
					} else if (classNames.contains("icon_pf_vlady")) {
						user.setVerifyType(WeiboUser.TYPE_VERIFY_LADY);
					} else {
						logger.warn("未知认证类型\"{}\" - {}", iconEles.get(0).className(), user.getId());
						return;
					}
				} else {
					throw new PageStructureException("iconEles.size() > 1");
				}

				// 认证信息
				int indexOfUserInfoHead = rawText.indexOf(
						"<script>FM.view({\"ns\":\"pl.content.homeFeed.index\",\"domid\":\"Pl_Core_UserInfo__");
				if (indexOfUserInfoHead == -1) {
					throw new PageStructureException("indexOfUserInfoHead == -1");
				}
				int indexOfUserInfoFoot = rawText.indexOf(")</script>", indexOfUserInfoHead);
				if (-1 == indexOfUserInfoFoot) {
					throw new PageStructureException("-1 == indexOfUserInfoFoot");
				}
				String userInfoHtml;
				try {
					JSObject jso = JSUtils
							.createJSObject(rawText.substring(indexOfUserInfoHead + 16, indexOfUserInfoFoot));
					userInfoHtml = jso.getNotNullString("html");
				} catch (JSException e) {
					throw new PageStructureException("pl.content.homeFeed.index解析JSON异常", e);
				}
				Document userInfoDoc = Jsoup.parse(userInfoHtml);
				if (user.getVerifyType() != WeiboUser.TYPE_VERIFY_NONE
						&& user.getVerifyType() != WeiboUser.TYPE_VERIFY_CLUB) {
					Elements infoEles = userInfoDoc.select("div.PCD_person_info > div.verify_area > p.info > span");
					if (infoEles.size() != 1) {
						throw new PageStructureException("infoEles.size() != 1");
					}
					user.setVerifyInfo(infoEles.get(0).text().trim());
				}

				// 基础信息
				Elements itemEles = userInfoDoc.select("div.PCD_person_info > div.WB_innerwrap ul.ul_detail > li.item");
				for (Element itemEle : itemEles) {
					Elements emis = itemEle.select(":root > span.item_ico > em");
					if (emis.isEmpty()) {
						emis = itemEle.select(":root > span.item_ico > i");
					}
					if (emis.size() != 1) {
						throw new PageStructureException("emis.size() != 1");
					}
					Set<String> classNames = emis.get(0).classNames();
					String itemText = itemEle.text().trim();
					if (classNames.contains("ficon_bag")) {
						// 行业类别
						if (itemText.startsWith("3 行业类别 ")) {
							user.setIndustryCategory(itemText.substring(7));
						}
					} else if (classNames.contains("ficon_cd_place")) {
						// 地域
						if (itemText.startsWith("2 ")) {
							user.setRegion(itemText.substring(2));
						} else {
							user.setRegion(itemText);
						}
					} else if (classNames.contains("ficon_pinfo")) {
						// 简介
						if (itemText.startsWith("Ü 简介： ")) {
							user.setPinfo(itemText.substring(6));
						}
					} else if (classNames.contains("pinfo_icon_baidu")) {
						// 百科
						if (null == user.getPinfo()) {
							user.setPinfo(itemText);
						}
						Elements baikeLinkEles = itemEle.select(":root > span.item_text > a");
						if (baikeLinkEles.size() == 1) {
							String baikeUrl = baikeLinkEles.get(0).attr("href").trim();
							if (baikeUrl.endsWith("/baike?from=infocard")) {
								user.setBaikeUrl(baikeUrl);
							}
						}
					}
				}
			}

			user.setStatus(WeiboUser.STATUS_NORMAL);

			if (isBaseTask) {
				logger.info("update user - {}", user);
				try {
					weiboUserService.update(user, String.valueOf(taskLevel * -1));
				} catch (ServiceException e) {
					logger.warn("update failed - " + user.getId(), e);
				}
			}

			// 微博头像下载
			if (null != user.getAvatarUrl()) {
				downloadHeaderUrl(user, currentRequest);
			} else {
				String avatarUrl = null;
				try {
					avatarUrl = getHeaderUrl(page);
				} catch (Exception e) {
					logger.error("getHeaderUrl failed - ", e);
				}
				if (StringUtils.isNotBlank(avatarUrl)) {
					user.setAvatarUrl(avatarUrl);
					downloadHeaderUrl(user, currentRequest);
				} else {
					logger.error("{}：无头像链接；", user.getNickName());
				}
			}
		}

		// 获取feedList
		String script_head = "<script>FM.view({\"ns\":\"pl.content.homeFeed.index\",\"domid\":\"Pl_Official_MyProfileFeed__";
		int indexFMViewScript = rawText.indexOf(script_head);
		if (-1 == indexFMViewScript) {
			throw new PageStructureException("-1 == indexFMViewScript");
		}
		int beginIndex;
		if (isFirstPage) {
			// 导航需要的参数
			beginIndex = indexFMViewScript + script_head.length();
			int endIndexOfPlName = rawText.indexOf("\",\"", beginIndex);
			if (-1 == endIndexOfPlName) {
				throw new PageStructureException("-1 == endIndexOfPlName");
			}
			currentRequest.putExtra(REQUEST_EXTRA_PL_NAME, rawText.substring(beginIndex, endIndexOfPlName));
		}

		beginIndex = indexFMViewScript + 16;
		int endIndexOfJsonText = rawText.indexOf(")</script>", indexFMViewScript);
		if (-1 == endIndexOfJsonText) {
			throw new PageStructureException("-1 == endIndexOfJsonText");
		}

		Document doc;
		try {
			JSObject jp = JSUtils.createJSObject(rawText.substring(beginIndex, endIndexOfJsonText));
			doc = Jsoup.parse(jp.getNotNullString("html"));
		} catch (JSException e) {
			throw new PageStructureException("NaviPage解析JSON异常", e);
		}

		Element feedList;
		try {
			feedList = doc.child(0).child(1).child(0);
		} catch (IndexOutOfBoundsException e) {
			throw new PageStructureException("NaviPage获取feedList失败", e);
		}

		processFeedList(page, feedList, isFirstPage);
	}

	private void downloadHeaderUrl(WeiboUser user, Request currentRequest) {
		if (null == user || null == currentRequest) {
			logger.error("null == WeiboUser || null == Request");
			return;
		}
		long weiboId = user.getId();
		String headerUrl = user.getAvatarUrl();
		if (isNeedToAssembly(headerUrl)) {
			headerUrl = "http:" + headerUrl;
		}
		String nowTime = String.valueOf(System.currentTimeMillis());
		Jedis jedis = null;
		if (MySpider.test) {
			jedis = JedisUtils.createJedis();
		}
		try {
			// 去重
			String key = REDIS_KEY_PREFIX_IMGID + weiboId;
			Boolean isExits = false;
			if (null == jedis) {
				isExits = JedisUtils.jc.exists(key);
				if (isExits) {// 重复
					logger.info("微博账户：-{}-的头像地址：-{}-已下载；", weiboId, headerUrl);
				}
			} else {
				isExits = jedis.exists(key);
				if (isExits) {// 重复
					logger.info("微博账户：-{}-的头像地址：-{}-已下载；", weiboId, headerUrl);
				}
			}
			org.slf4j.Logger statisticLogger = org.slf4j.LoggerFactory
					.getLogger("com.cmcc.wltx.collector.statistics.weibo.home.headerUrl");
			if (!isExits) {
				statisticLogger.info("{}\t{}\t{}\t{}", nowTime, user.getId(), user.getNickName(), headerUrl);
			}
		} catch (Exception e) {
			logger.error("downloadHeaderUrl下载微博头像", e);
		} finally {
			if (null != jedis) {
				jedis.close();
			}
		}
	}

	/**
	 * 获取头像链接
	 * 
	 * @param page
	 * @return
	 */
	private String getHeaderUrl(Page page) throws Exception {
		String rawText = page.getRawText();
		// 头像链接
		int indexOfHeaderHead = rawText
				.indexOf("<script>FM.view({\"ns\":\"pl.header.head.index\",\"domid\":\"Pl_Official_Headerv6__1\"");
		if (indexOfHeaderHead == -1) {
			indexOfHeaderHead = rawText.indexOf("<script>FM.view({\"ns\":\"pl.header.preloginHead.index\"");
			if (indexOfHeaderHead == -1) {
				logger.error("getHeaderUrl：indexOfHeaderHead == -1");
				return null;
			}
		}
		int indexOfHeaderFoot = rawText.indexOf(")</script>", indexOfHeaderHead);
		if (-1 == indexOfHeaderFoot) {
			logger.error("getHeaderUrl：-1 == indexOfHeaderFoot");
			return null;
		}
		String headerHtml;
		try {
			JSObject jso = JSUtils.createJSObject(rawText.substring(indexOfHeaderHead + 16, indexOfHeaderFoot));
			headerHtml = jso.getNotNullString("html");
		} catch (JSException e) {
			logger.error("getHeaderUrl：pl.header.head.index解析JSON异常", e);
			return null;
		}
		Document headerDoc = Jsoup.parse(headerHtml);
		Elements avatarEles = headerDoc.select("div.pf_photo > p.photo_wrap > img.photo");
		if (1 != avatarEles.size()) {
			logger.error("getHeaderUrl：1 != avatarEles.size()");
			return null;
		}
		String avatarUrl = avatarEles.get(0).attr("src").trim();
		if (avatarUrl.isEmpty()) {
			logger.error("getHeaderUrl：avatarUrl.isEmpty()");
			return null;
		}
		return avatarUrl;
	}

	private void processFeedList(Page page, Element feedList, boolean isFirstPage)
			throws PageProcessException, PageStructureException {
		Request currentRequest = page.getRequest();
		WeiboUser user = (WeiboUser) currentRequest.getExtra(REQUEST_EXTRA_WEIBO_USER);
		List<Article> articles = new ArrayList<Article>();
		boolean haveMore = true;
		long now = System.currentTimeMillis();
		Jedis jedis = null;
		if (MySpider.test) {
			jedis = JedisUtils.createJedis();
		}
		try {
			for (Element feedEle : feedList.children()) {
				String mid = feedEle.attr("mid").trim();
				if (mid.length() == 0) {
					continue;
				}

				Elements wbDetailEles = feedEle.select(":root > div[node-type=feed_content] > div.WB_detail");
				if (1 != wbDetailEles.size()) {
					throw new PageStructureException("wbDetailEles.size()=" + wbDetailEles.size() + " - " + mid);
				}
				Element wbDetailEle = wbDetailEles.get(0);

				// 判断是否置顶
				boolean isTopFeed = false;
				Elements W_icon_feedpin = wbDetailEle.select("span.W_icon_feedpin");
				if (W_icon_feedpin.size() != 0) {
					if (W_icon_feedpin.get(0).ownText().contains("置顶")) {
						isTopFeed = true;
					}
				}

				// 去重
				String key = REDIS_KEY_PREFIX_MID + mid;
				if (null == jedis) {
					JedisCluster jedisCluster = null;
					try {
						jedisCluster = JedisUtils.jc;
						if (null != jedisCluster) {
							Long setnx = jedisCluster.setnx(key, "");
							if (0 == setnx) {// 重复
								if (isTopFeed) { // 置顶
									continue;
								}
								// 重复且不是置顶，说明后面也都是采集过的，没必要继续了
								haveMore = false;
								break;
							}
							jedisCluster.expire(key, EXPIRE);
						} else {
							logger.info("processFeedList -- JedisCluster find error");
							break;
						}
					} catch (Exception e) {
						logger.error("processFeedList -- JedisCluster find error", e);
						break;
					}
				} else {
					Long setnx = jedis.setnx(key, "");
					if (0 == setnx) {
						if (isTopFeed) {
							continue;
						}
						haveMore = false;
						break;
					}
					jedis.expire(key, EXPIRE);
				}

				// 时间
				Elements dateEles = wbDetailEle.select(":root > div.WB_from > a[date~=\\d{13,}]");
				if (dateEles.size() != 1) {
					throw new PageStructureException("dateEles.size()=" + dateEles.size() + " - " + mid);
				}
				long pubTime = Long.parseLong(dateEles.get(0).attr("date"));
				if (now - pubTime > EXPIRE_M) { // 过期
					if (isTopFeed) {
						// 跳过过期的置顶微博
						continue;
					}
					// 过期且不是置顶，说明再往后也都是过期的，没必要继续了
					haveMore = false;
					break;
				}

				// url
				Article article = createArticle(WeiBoUtils.getReference(user.getId().toString(), mid), 2);
				article.setTitle("新浪微博");
				article.setCreateDate(new Date(pubTime));

				// 作者
				article.setAuthor(user.getNickName());

				// 正文
				Elements contentEles = wbDetailEle.select(":root > div.WB_text");
				if (contentEles.size() != 1) {
					throw new PageStructureException("contentEles.size()=" + contentEles.size() + " - " + mid);
				}
				String content = contentEles.get(0).text();
				// 转发部分
				String omid = feedEle.attr("omid");
				if (omid.length() != 0) {
					Elements expandContentEles = wbDetailEle
							.select(":root > div.WB_feed_expand > div.WB_expand > div.WB_text");
					if (expandContentEles.size() == 1) {
						content = content + " Fw:" + expandContentEles.get(0).text();
					}
				}
				article.setContent(content);

				articles.add(article);
			}
		} finally {
			if (null != jedis) {
				jedis.close();
			}
		}

		int fromLevel = (Integer) currentRequest.getExtra(REQUEST_EXTRA_TASK_LEVEL);
		boolean isBaseTask = fromLevel < 0;
		if (isFirstPage && !isBaseTask && fromLevel <= 2) {
			taskService.transferLevel(user, fromLevel, articles.size() == 0 ? 2 : 3);
		}
		if (articles.size() == 0) {
			return;
		} else if (fromLevel >= 3) {
			org.slf4j.Logger statisticLogger = org.slf4j.LoggerFactory
					.getLogger("com.cmcc.wltx.collector.statistics.weibo.home." + fromLevel);
			statisticLogger.info("{}\t{}\t{}\t{}", now, user.getId(), user.getFanCount(), articles.size());
		}
		page.setSkip(false);
		page.putField(ArticlesOutFileWriterPipeline.KEY_ARTICLES, articles);

		if (!haveMore) {
			return;
		}

		// 导航
		String naviUrl = null;
		int pagebar = (Integer) currentRequest.getExtra(REQUEST_EXTRA_PAGEBAR);
		int pagenum = (Integer) currentRequest.getExtra(REQUEST_EXTRA_PAGE_NUM);
		String page_id = (String) currentRequest.getExtra(REQUEST_EXTRA_PAGE_ID);
		String domain = (String) currentRequest.getExtra(REQUEST_EXTRA_DOMAIN);
		String plName = (String) currentRequest.getExtra(REQUEST_EXTRA_PL_NAME);

		int requestType;
		if (pagebar < 1) {
			requestType = REQUEST_TYPE_FALLS;
			pagebar++;
			if (pagenum == 1) {
				naviUrl = "http://weibo.com/p/aj/v6/mblog/mbloglist?ajwvr=6&domain=" + domain + "&is_all=1&pagebar="
						+ pagebar + "&pl_name=Pl_Official_MyProfileFeed__" + plName + "&id=" + page_id
						+ "&script_uri=/u/" + user.getId() + "&feed_type=0&page=1&pre_page=1&domain_op=" + domain
						+ "&__rnd=" + System.currentTimeMillis();
			} else {
				naviUrl = "http://weibo.com/p/aj/v6/mblog/mbloglist?ajwvr=6&domain=" + domain
						+ "&is_search=0&visible=0&is_all=1&is_tag=0&profile_ftype=1&page=" + pagenum + "&pagebar="
						+ pagebar + "&pl_name=Pl_Official_MyProfileFeed__" + plName + "&id=" + page_id
						+ "&script_uri=/u/" + user.getId() + "&feed_type=0&pre_page=" + pagenum + "&domain_op=" + domain
						+ "&__rnd=" + System.currentTimeMillis();
			}
		} else {
			requestType = REQUEST_TYPE_NAVI;
			pagebar = -1;
			pagenum++;
			naviUrl = "http://weibo.com/u/" + user.getId() + "?pids=Pl_Official_MyProfileFeed__" + plName
					+ "&is_search=0&visible=0&is_all=1&is_tag=0&profile_ftype=1&page=" + pagenum + "#feedtop";
		}

		Request nextRequest = new Request(naviUrl);
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_HEADERS,
				currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_HEADERS));
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY,
				currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY));
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, requestType);
		nextRequest.putExtra(REQUEST_EXTRA_WEIBO_USER, user);
		nextRequest.putExtra(REQUEST_EXTRA_TASK_LEVEL, fromLevel);
		nextRequest.putExtra(REQUEST_EXTRA_PAGEBAR, pagebar);
		nextRequest.putExtra(REQUEST_EXTRA_PAGE_NUM, pagenum);
		nextRequest.putExtra(REQUEST_EXTRA_PAGE_ID, page_id);
		nextRequest.putExtra(REQUEST_EXTRA_DOMAIN, domain);
		nextRequest.putExtra(REQUEST_EXTRA_PL_NAME, plName);
		if (null != currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE)) {
			nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE,
					currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE));
		}
		
		int proxyChange = 0;// 遵从当前代理是否是切换的免费代理
		if (null != currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE)) {
			proxyChange = Integer.valueOf(currentRequest.getExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE).toString());
		}
		nextRequest.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_CHANGE, proxyChange);
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
		MySpider.test = spiderId.startsWith(MySpider.PREFIX_ID_TEST);
		// 获取配置
		SpiderService spiderService = ServiceFactory.getSpiderService();
		MySite site;
		try {
			site = spiderService.buildMySiteNew(newSpiderIds);
			site.setStoreCookie(false);
		} catch (ServiceException e) {
			logger.error("spider config load error", e);
			return;
		}

		if (MySpider.test) {
			// 初始化redis
			String redisHost = site.getRedisHost();
			int redisPort = site.getRedisPort();
			if (null == redisHost || redisHost.length() == 0 || redisPort <= 0) {
				logger.error("redis config error");
				return;
			}
			JedisUtils.initPool(redisHost, redisPort);
		} else {
			JedisUtils.initPoolBySentinel();
		}

		MySpider spider = null;
		try {
			// 创建爬虫
			HomeWeiboSpiderTaskService taskService = ServiceFactory.getHomeWeiboSpiderTaskService();
			WeiboVisitorCookieService cookieService = ServiceFactory.getWeiboVisitorCookieService();
			spider = MySpider.create(new HomeWeiboPageProcessor(site), null, new QueueScheduler(), proxyType,
					new ArticlesOutFileWriterPipeline(site.getFilePipelinePath(), false, 100));
			if (proxyType == 3) {// 付费、免费混合使用
				((MyHttpClientDownloader) spider.getDownloader()).initProxy(2);
			}
			do {
				// 获取微博任务取值频率
				double[] weights = null;
				try {
					weights = cookieService.installWeiboTaskWeights(proxyType);
				} catch (ServiceException e) {
					logger.error("get weiboTask weights failed", e);
					return;
				}
				if (null == weights) {
					logger.error("no weiboTask weights");
					break;
				}

				// 获取任务
				List<HomeWeiboSpiderTask> tasksForCrawl;
				try {
					tasksForCrawl = taskService.tasksForSpider(spiderId, site.getTaskLimit(), weights);
				} catch (ServiceException e) {
					logger.error("get tasks for crawl failed", e);
					return;
				}
				if (0 == tasksForCrawl.size()) {
					logger.info("no task for crawl");
					break;
				}
				List<String> cookies;
				try {
					cookies = cookieService.getAllCookies();
				} catch (ServiceException e) {
					logger.error("get cookies failed", e);
					return;
				}
				int cookieSize = cookies.size();
				if (0 == cookieSize) {
					logger.error("no cookies");
					break;
				}

				for (HomeWeiboSpiderTask task : tasksForCrawl) {
					Request request = new Request("http://weibo.com/u/" + task.getId() + "?is_all=1");
					WeiboUser user = new WeiboUser();
					user.setId(task.getId());
					request.putExtra(REQUEST_EXTRA_WEIBO_USER, user);
					request.putExtra(REQUEST_EXTRA_TASK_LEVEL, task.getLevel());
					request.putExtra(ConstantsHome.REQUEST_EXTRA_TYPE, REQUEST_TYPE_START);
					request.putExtra(REQUEST_EXTRA_PAGEBAR, -1);
					request.putExtra(REQUEST_EXTRA_PAGE_NUM, 1);
					Map<String, String> requestHeaders = new HashMap<String, String>();
					requestHeaders.put(ConstantsHome.REQUEST_HEADER_COOKIE,
							cookies.get(RandomUtils.nextInt(cookieSize)));
					request.putExtra(ConstantsHome.REQUEST_EXTRA_HEADERS, requestHeaders);
					// 标注代理类型
					request.putExtra(ConstantsHome.REQUEST_EXTRA_PROXY_TYPE, proxyType);
					spider.addRequest(request);
					logger.info("start task - {}", request.getUrl());
				}

				spider.run();
				if (!ConstantsHome.FINISH.exists()) {
					ConstantsHome.FINISH.mkdir();
				}
				try {
					VerticalOutFileWriter.closeAll();
				} catch (IOException e) {
					logger.error("tmp转out文件失败", e);
					break;
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
				if (proxyType == 2 || proxyType == 3) {// 重新记载免费代理
					((MyHttpClientDownloader) spider.getDownloader()).initProxy(2);
				}
			} while (true);
		} finally {
			try {
				VerticalOutFileWriter.closeAll();
			} catch (IOException e) {
				logger.error("tmp转out文件失败", e);
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

	/**
	 * 是否需要组装url
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isNeedToAssembly(String url) {
		if (url.startsWith("http://") || url.startsWith("https://")) {
			return false;
		}
		return true;
	}
}