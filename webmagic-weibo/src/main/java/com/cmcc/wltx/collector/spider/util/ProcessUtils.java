package com.cmcc.wltx.collector.spider.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.model.Article;

public class ProcessUtils {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProcessUtils.class);
	private static final String REGEX_LAYER = "(https?://(?!{c}\\.)[^/?]+{d0}|https?://{c}\\.[^/?]+{d1})([./?].*)?";
	private static final String REGEX_COUNTRY = "(c[nacdfhiklmoqruvxyz]|a[adefgilmnorstuwz]|b[abdefghijmnorstvwyz]|d[ejkmoz]|e[ceghrstuv]|f[ijkmor]|g[adefghilmnprstuwy]|h[kmnrtu]|i[delmnoqrst]|j[emop]|k[eghimnprwyz]|l[abcikrstuvy]|m[acdeghklmnopqrstuvwxyz]|n[acefgiloprtuz]|om|p[aefghklmnrtwy]|qa|r[eosuw]|s[abcdeghijklmnortuvxyz]|t[cdfghjklmnortvwz]|u[agkmsyz]|v[aceginu]|w[fs]|y[et]|z[amw])";
	private static final String REGEX_URL = "https?://(?<ip>\\d{1,3}(\\.\\d{1,3}){3}|(?<channel>([\\w-]+\\.)*)(?!(?<s>(com|net|gov|org|edu|aero|army|arts|biz|cc|co|coop|europa|firm|fm|gc|idv|im|info|int|me|mil|museum|name|nom|pro|rec|store|tv|tx|web|travel|xxx|site|mobi|wang|ren|citic|zj|js)(\\."
			+ REGEX_COUNTRY + ")?|" + REGEX_COUNTRY + ")([#?/:]|$))(?<domain>[\\w-]+\\.\\k<s>))($|[#?/:].*)";

	public static org.w3c.dom.Document htmlToXml(String page)
			throws ParserConfigurationException, SAXException, IOException {
		if (null == page) {
			return null;
		}
		Pattern pa = Pattern.compile("<html(\\s+[^>]*)?>.*</html>", Pattern.DOTALL);
		Matcher ma = pa.matcher(page);
		if (!ma.find()) {
			// throw new org.dom4j.DocumentException("找不到html标签");
		}
		String html = ma.group();
		html = html.replaceAll("(?s)<script(\\s+[^>]*)?>.*?</script>", "");
		html = html.replaceAll("(?s)<style(\\s+[^>]*)?>.*?</style>", "");
		html = html.replaceAll("<link(\\s+[^>]*)?>", "");
		// html = html.replaceAll("<!--[^>-]*-->", "");
		// return DocumentHelper.parseText(html);
		StringReader sr = new StringReader(html);
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(is);
	}

	public static boolean judgePageLink(String link) {
		// 转小写
		link = link.toLowerCase();

		// http://ipo.csrc.gov.cn/pdfdownload.action?ipoCode=732526198&xmlId=1
		// http://weblbs.cc.163.com/cc-download
		if (link.contains("download")) {
			return false;
		}

		// http://tech.ifeng.com/{{$item.document.extraLink|default:$item.url}}
		if (link.indexOf('{') >= 0) {
			return false;
		}

		// http://sjbdy.ycwb.com/wap/index.php?mdl=android
		// http://sjbdy.ycwb.com/wap/index.php?mdl=iphone
		if (link.endsWith("?mdl=android") || link.endsWith("?mdl=iphone")) {
			return false;
		}

		// 去掉参数
		int indexOfInterrogation = link.indexOf('?');
		if (indexOfInterrogation > 0) {
			link = link.substring(0, indexOfInterrogation);
		}

		// 开始判断
		// .(jpg|png|gif|bmp|docx|doc|xls|xlsx|swf|ppt|flv|mp3|avi|mp4|mov|mpeg|mpg|rm|wmv|rmvb|3gp|aac|m4v|f4v|wav|mlv|ac3|ogg|dat|asf|flac|ape|m4a|mid|cda|aif|pic|mkv|zip|7z|rar|gz|tar.gz|tar|jar|iso|pdf|exe|dll|bin|apk)
		if (link.endsWith(".jpg") || link.endsWith(".png") || link.endsWith(".gif") || link.endsWith(".bmp")
				|| link.endsWith(".docx") || link.endsWith(".doc") || link.endsWith(".xls") || link.endsWith(".xlsx")
				|| link.endsWith(".swf") || link.endsWith(".ppt") || link.endsWith(".flv") || link.endsWith(".mp3")
				|| link.endsWith(".avi") || link.endsWith(".mp4") || link.endsWith(".mov") || link.endsWith(".mpeg")
				|| link.endsWith(".mpg") || link.endsWith(".rm") || link.endsWith(".wmv") || link.endsWith(".rmvb")
				|| link.endsWith(".3gp") || link.endsWith(".aac") || link.endsWith(".m4v") || link.endsWith(".f4v")
				|| link.endsWith(".wav") || link.endsWith(".mlv") || link.endsWith(".ac3") || link.endsWith(".ogg")
				|| link.endsWith(".dat") || link.endsWith(".asf") || link.endsWith(".flac") || link.endsWith(".ape")
				|| link.endsWith(".m4a") || link.endsWith(".mid") || link.endsWith(".cda") || link.endsWith(".aif")
				|| link.endsWith(".pic") || link.endsWith(".mkv") || link.endsWith(".zip") || link.endsWith(".7z")
				|| link.endsWith(".rar") || link.endsWith(".gz") || link.endsWith(".tar.gz") || link.endsWith(".tar")
				|| link.endsWith(".jar") || link.endsWith(".iso") || link.endsWith(".pdf") || link.endsWith(".exe")
				|| link.endsWith(".dll") || link.endsWith(".bin") || link.endsWith(".apk")) {
			return false;
		}

		// http://auto.sohu.com/20061008/mailto:jubao@contact.sohu.com
		if (Pattern.compile("/mailto:\\w+@").matcher(link).find()) {
			return false;
		}

		return true;
	}

	public static boolean judgePageLinkToShangJi(String link) {
		// 转小写
		link = link.toLowerCase();

		if (link.startsWith("?") || link.startsWith("&")) {
			return false;
		}

		// http://ipo.csrc.gov.cn/pdfdownload.action?ipoCode=732526198&xmlId=1
		// http://weblbs.cc.163.com/cc-download
		if (link.contains("download")) {
			return false;
		}

		// http://tech.ifeng.com/{{$item.document.extraLink|default:$item.url}}
		if (link.indexOf('{') >= 0) {
			return false;
		}

		// http://sjbdy.ycwb.com/wap/index.php?mdl=android
		// http://sjbdy.ycwb.com/wap/index.php?mdl=iphone
		if (link.endsWith("?mdl=android") || link.endsWith("?mdl=iphone")) {
			return false;
		}

		// 开始判断
		// .(jpg|png|gif|bmp|docx|doc|xls|xlsx|swf|ppt|flv|mp3|avi|mp4|mov|mpeg|mpg|rm|wmv|rmvb|3gp|aac|m4v|f4v|wav|mlv|ac3|ogg|dat|asf|flac|ape|m4a|mid|cda|aif|pic|mkv|zip|7z|rar|gz|tar.gz|tar|jar|iso|pdf|exe|dll|bin|apk)
		if (link.endsWith(".jpg") || link.endsWith(".png") || link.endsWith(".gif") || link.endsWith(".bmp")
				|| link.endsWith(".docx") || link.endsWith(".doc") || link.endsWith(".xls") || link.endsWith(".xlsx")
				|| link.endsWith(".swf") || link.endsWith(".ppt") || link.endsWith(".flv") || link.endsWith(".mp3")
				|| link.endsWith(".avi") || link.endsWith(".mp4") || link.endsWith(".mov") || link.endsWith(".mpeg")
				|| link.endsWith(".mpg") || link.endsWith(".rm") || link.endsWith(".wmv") || link.endsWith(".rmvb")
				|| link.endsWith(".3gp") || link.endsWith(".aac") || link.endsWith(".m4v") || link.endsWith(".f4v")
				|| link.endsWith(".wav") || link.endsWith(".mlv") || link.endsWith(".ac3") || link.endsWith(".ogg")
				|| link.endsWith(".dat") || link.endsWith(".asf") || link.endsWith(".flac") || link.endsWith(".ape")
				|| link.endsWith(".m4a") || link.endsWith(".mid") || link.endsWith(".cda") || link.endsWith(".aif")
				|| link.endsWith(".pic") || link.endsWith(".mkv") || link.endsWith(".zip") || link.endsWith(".7z")
				|| link.endsWith(".rar") || link.endsWith(".gz") || link.endsWith(".tar.gz") || link.endsWith(".tar")
				|| link.endsWith(".jar") || link.endsWith(".iso") || link.endsWith(".pdf") || link.endsWith(".exe")
				|| link.endsWith(".dll") || link.endsWith(".bin") || link.endsWith(".apk")) {
			return false;
		}

		// http://auto.sohu.com/20061008/mailto:jubao@contact.sohu.com
		if (Pattern.compile("/mailto:\\w+@").matcher(link).find()) {
			return false;
		}

		return true;
	}

	public static final String PLACEHOLDER_DOMAIN = "{domain}";
	public static final String REGEX_DEEP_0 = "https?://(\\w+\\.)?{domain}(/(index\\.(s?html?|php|jsp))?)?";
	public static final String REGEX_DEEP_1 = "https?://((\\w+\\.)?{domain}[/?]\\D+|(\\w+\\.){2,}{domain}([/?]\\D*)?)";

	public static int calculateDeep(String url, String domain) {
		if (null == domain || domain.length() == 0) {
			return -1;
		}
		domain = domain.replace(".", "\\.");

		if (url.matches(REGEX_DEEP_0.replace(PLACEHOLDER_DOMAIN, domain))) {
			return 0;
		}

		if (url.matches(REGEX_DEEP_1.replace(PLACEHOLDER_DOMAIN, domain))) {
			return 1;
		}

		return -1;
	}

	/**
	 * 提取url中的domain部分
	 * 
	 * @param url
	 * @return 非empty字符串或null(当无法提取时)
	 */
	public static String extractDomain(String url) {
		Pattern pa = Pattern.compile(REGEX_URL);
		Matcher ma = pa.matcher(url.toLowerCase());
		if (ma.matches()) {
			String domain = ma.group("domain");
			if (null == domain) {
				return ma.group("ip");
			} else {
				return domain;
			}
		} else {
			return null;
		}
	}

	public static String extractLayer(String url, String thirdDomain, int dc0, int dc1) {
		if (null == thirdDomain || thirdDomain.length() == 0) {
			thirdDomain = "www";
		}
		String d0 = "";
		String d1 = "/[^./?]+";
		if (dc0 > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < dc0; i++) {
				sb.append(d1);
			}
			d0 = sb.toString();
		}
		if (dc1 > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < dc1; i++) {
				sb.append(d1);
			}
			d1 = sb.append(d1).toString();
		}

		String regex = REGEX_LAYER.replace("{c}", thirdDomain).replace("{d0}", d0).replace("{d1}", d1);
		Pattern pa = Pattern.compile(regex);
		Matcher ma = pa.matcher(url);
		if (ma.matches()) {
			return ma.group(1);
		}
		return null;
	}

	public static String extractChannel(String url) {
		Pattern pa = Pattern.compile(REGEX_URL);
		Matcher ma = pa.matcher(url);
		if (ma.matches()) {
			String group = ma.group(3);
			if (null != group && group.length() != 0) {
				return group.substring(0, group.length() - 1);
			}
		}
		return null;
	}

	public static String removeCdataTag(String source) {
		if (source.startsWith("<![CDATA[") && source.endsWith("]]>")) {
			return source.substring(9, source.length() - 3);
		} else {
			return source;
		}
	}

	public static String getAllTextFromPage(String source, String xpath, Request req) {
		Page contentPage = new Page();
		contentPage.setRawText(source);
		contentPage.setRequest(req);
		return contentPage.getHtml().xpath(xpath).toString();
	}

	public static Request createAppChannelPageRequest(String url) {
		Request req = new Request(url, true);
		if (url.endsWith("&") && url.contains("?")) {
			String[] split = url.split("\\?");
			if (split.length != 2) {
				throw new RuntimeException("URL格式错误 - " + url);
			}
			req.setMethod(HttpConstant.Method.POST);

			String[] pairs = split[1].split("&");
			NameValuePair[] params = new BasicNameValuePair[pairs.length];
			for (int i = 0; i < pairs.length; i++) {
				String[] pair = pairs[i].split("=");
				if (pair.length == 1) {
					params[i] = new BasicNameValuePair(pair[0], null);
				} else if (pair.length == 2) {
					try {
						params[i] = new BasicNameValuePair(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new Error("不支持UTF-8编码", e);
					}
				} else {
					throw new RuntimeException("URL格式错误 - " + url);
				}
			}

			Map<String, Object> extras = new HashMap<String, Object>();
			extras.put(ConstantsHome.NAME_VALUE_PAIR, params);
			req.setExtras(extras);
		}
		return req;
	}

	public static final Pattern forumPa = Pattern.compile("/forum-\\d+-1\\.");
	public static final Pattern paperPa = Pattern
			.compile("\\D(20|19)\\d{2}[/_-]?(0[1-9]|1[0-2])[/_-]?(0[1-9]|[12]\\d|3[01])/node_");
	public static final Pattern threadPa = Pattern.compile("/thread-\\d+-1-1\\.");
	public static final Pattern datePa = Pattern
			.compile("\\D(20|19)\\d{2}[/_-]?(0[1-9]|1[0-2])[/_-]?(0[1-9]|[12]\\d|3[01])");
	public static final Pattern datePa1 = Pattern
			.compile("\\D(20|19)\\d{2}[/_-]?([1-9]|1[0-2])[/_-]?([1-9]|[12]\\d|3[01])\\D");
	private static final Pattern naviPa = Pattern.compile("https?://[^/?]+([/?]\\D*(2017\\D*)?)?");
	private static final Pattern warnPa = Pattern.compile("/(?![a-z]*[./])[\\w-]{8,}");
	// private static final String[] naviKeywords = {"articles", "index",
	// "list", "tag", "search", "keyword", "special", "topic"};
	private static final Pattern naviKeywordsPa = Pattern
			.compile("[^a-zA-Z](articles|index|list|node|channels?|tags?|search|keywords?)([^a-zA-Z]|$)");

	public static boolean containNaviKeyword(String url) {
		if (url.length() < 8) {
			return false;
		}
		int indexOf = url.indexOf('/', 8);
		if (indexOf < 0) {
			indexOf = url.indexOf('?', 8);
			if (indexOf < 0) {
				return false;
			}
		}
		url = url.toLowerCase();
		if (naviKeywordsPa.matcher(url.substring(indexOf)).find()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为导航URL：根据sourceType进行正则匹配 </br>
	 * <b>OR</b> 根据导航URL正则匹配 </br>
	 * <b>OR</b> URL中是否包含关键词：articles,list,node...
	 * 
	 * @param url
	 * @param sourceType
	 * @return
	 */
	public static boolean isNaviUrl(String url, int sourceType) {
		url = url.toLowerCase();
		if (Article.RECTYPE_PAPER == sourceType) {
			Matcher matcher = ProcessUtils.paperPa.matcher(url);
			if (matcher.find()) {
				String datePart = matcher.group();
				datePart = datePart.replaceAll("\\D", "");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				if (sdf.format(new Date()).equals(datePart)) {
					return true;
				}
			}
			return false;
		}
		if (Article.RECTYPE_BBS == sourceType) {
			if (ProcessUtils.forumPa.matcher(url).find()) {
				return true;
			}
		}
		if (naviPa.matcher(url).matches()) {
			return true;
		}
		if (containNaviKeyword(url)) {
			return true;
		}
		return false;
	}

	private static final String REGEX_KEYWORDS_IGNORE_HEAD = "https?://([\\w-]+\\.)*(";
	private static final String REGEX_KEYWORDS_IGNORE_FOOT = ")(\\.[\\w-]+)*([/?].*)?";
	private static final String REGEX_KEYWORDS_IGNORE_HEAD_ = "https?://([^/]+/)+(";
	private static final String REGEX_KEYWORDS_IGNORE_FOOT_ = ")([/?.].*)?";
	private static Pattern[] newsIgnorePatterns;
	private static Pattern[] bbsIgnorePatterns;
	private static Pattern[] blogIgnorePatterns;
	private static Pattern[] paperIgnorePatterns;
	/**
	 * 匹配汉字的正则
	 */
	private static final Pattern PATTERN_CH = Pattern.compile("[一-龥]");

	public static boolean commonIgnore(String url, int sourceType) {
		if (PATTERN_CH.matcher(url).find()) {
			return true;
		}
		char[] charArray = url.toCharArray();
		int scount = 0;
		for (char c : charArray) {
			if (c == '/') {
				scount++;
			}
		}
		if (scount >= 12) {
			return true;
		}

		url = url.toLowerCase();
		switch (sourceType) {
		case Article.RECTYPE_BBS:
			if (null != bbsIgnorePatterns) {
				for (Pattern pa : bbsIgnorePatterns) {
					if (pa.matcher(url).matches()) {
						return true;
					}
				}
			}
			break;
		case Article.RECTYPE_BLOG:
			if (null != blogIgnorePatterns) {
				for (Pattern pa : blogIgnorePatterns) {
					if (pa.matcher(url).matches()) {
						return true;
					}
				}
			}
			break;
		case Article.RECTYPE_PAPER:
			if (null != paperIgnorePatterns) {
				for (Pattern pa : paperIgnorePatterns) {
					if (pa.matcher(url).matches()) {
						return true;
					}
				}
			}
			break;
		default:
			if (null != newsIgnorePatterns) {
				for (Pattern pa : newsIgnorePatterns) {
					if (pa.matcher(url).matches()) {
						return true;
					}
				}
			}
			break;
		}

		return false;
	}

	private static final String PARAM_SITEMAP = "?sitemap";
	private static final Pattern PATTERN_PARAM_FROM = Pattern.compile("([^?]+)\\?from=[^&]*&?");

	/**
	 * 处理URL：去空格、锚点、去掉尾部?sitemap、去掉 ?from=、去掉尾部? 等
	 * 
	 * @param url
	 * @return 处理后的URL
	 * @throws IOException
	 */
	public static String processUrl(String url) throws IOException {
		// 去空格换行
		url = url.replaceAll("\\s", "");

		// 去掉锚点
		int indexOfPound = url.indexOf('#');
		if (indexOfPound > 0) {
			url = url.substring(0, indexOfPound);
		}

		// 处理相对路径
		boolean pathEndsWithSlash = false;
		int indexOfPathStart = url.indexOf('/', 8);
		if (-1 != indexOfPathStart) {
			int indexOfPathEnd = url.indexOf("?");
			if (-1 == indexOfPathEnd) {
				indexOfPathEnd = url.length();
			}
			String path = url.substring(indexOfPathStart, indexOfPathEnd);
			if (path.charAt(path.length() - 1) == '/') {
				pathEndsWithSlash = true;
			}

			File file = new File(path.replaceAll("/{2,}", "/"));
			String canonicalPath = file.getCanonicalPath();
			if (File.separatorChar == '\\') {
				int indexOfFirstSeparatorChar = canonicalPath.indexOf(File.separatorChar);
				canonicalPath = canonicalPath.substring(indexOfFirstSeparatorChar).replace(File.separatorChar, '/');
			}
			url = url.substring(0, indexOfPathStart) + canonicalPath
					+ (pathEndsWithSlash && canonicalPath.length() != 1 ? '/' : "") + url.substring(indexOfPathEnd);
		}

		// 去掉尾部的“?sitemap”
		if (url.endsWith(PARAM_SITEMAP)) {
			url = url.substring(0, url.length() - PARAM_SITEMAP.length());
		}

		// http://love.163.com?from=sitemap 去掉 ?from=
		Matcher ma = PATTERN_PARAM_FROM.matcher(url);
		if (ma.matches()) {
			url = ma.group(1);
		}

		// 如果以?结尾的话，去掉?
		int lastIndex = url.length() - 1;
		if (url.charAt(lastIndex) == '?') {
			url = url.substring(0, lastIndex);
		}

		return url;
	}

	private static String[] uncorrelatedNames;
	private static String[] uncorrelatedWords;

	/**
	 * 查看name是否命中t_properties表c_name中array.comma.keywords.ignore.linkname.
	 * contain对应的词
	 * <p>
	 * 去掉藏品、娱乐、地产等频道链接
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isUncorrelatedName(String name) {
		if (null != uncorrelatedWords) {
			for (String word : uncorrelatedWords) {
				if (name.contains(word)) {
					return true;
				}
			}
		}
		if (null != uncorrelatedNames) {
			for (String uncorrelatedName : uncorrelatedNames) {
				if (uncorrelatedName.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Date parseDate(String source) throws ParseException {
		source = source.replaceAll("\\D+", " ").trim();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH mm ss");
		Date date;
		try {
			date = sdf.parse(source);
		} catch (ParseException e) {
			sdf = new SimpleDateFormat("yyyy MM dd HH mm");
			try {
				date = sdf.parse(source);
			} catch (ParseException e1) {
				sdf = new SimpleDateFormat("yyyy MM dd");
				date = sdf.parse(source);
			}
		}
		return date;
	}

	/**
	 * 自动化抽取论坛
	 * 
	 * @param page
	 * @param article
	 */
	public static void extractPost(Page page, com.cmcc.wltx.collector.spider.model.Article article) {
		Html html = page.getHtml();

		// 标题
		Selectable titleSel = html.xpath("//*[@id='thread_subject']/text()");
		if (titleSel.match()) {
			String title = titleSel.get().trim();
			if (null != title && title.length() != 0) {
				article.setTitle(title);
			}
		} else {
			return;
		}

		// 正文、日期
		// http://bbs.0513.org/thread-3571151-1-1.html
		// http://bbs.0566cc.cc/thread-178588-1-1.html
		Selectable postSel = html.xpath("//*[@id='postlist']/div[contains(@ID,'post_')]");
		if (postSel.match()) {
			List<Selectable> nodes = postSel.nodes();
			Selectable postmessageSel = nodes.get(0).xpath("//*[contains(@ID,'postmessage_')]");
			if (postmessageSel.match()) {
				String content = postmessageSel.nodes().get(0).xpath("//allText()").get().trim();
				if (null != content && content.length() != 0) {
					article.setContent(content);
				}
			}

			Selectable emSel = nodes.get(0).xpath("//*[contains(@ID,'authorposton')]");
			if (emSel.match()) {
				String date = null;
				Selectable titSel = emSel.nodes().get(0).xpath("//span[1]/@title");
				if (titSel.match()) {
					date = titSel.get().trim();
				} else {
					date = emSel.nodes().get(0).xpath("/*/text()").get().trim();
				}
				if (null != date && date.length() != 0) {
					Date createDate = null;
					try {
						createDate = ProcessUtils.parseDate(date);
					} catch (ParseException e) {
						logger.warn("unparseable date : \"{}\" - {}", date, article.getUrl());
					}
					article.setCreateDate(createDate);
				}
			}
		} else {
			return;
		}

		// 来源
		Selectable sourceSel = html.xpath("//meta[@name='application-name']/@content");
		if (sourceSel.match()) {
			String source = sourceSel.get().trim();
			if (null != source && source.length() != 0) {
				article.setPubSource(source);
			}
		}
	}

	private static Pattern sourceHeadPa = Pattern.compile("(来源于?|出处|稿源|来自于)[：:]");
	private static Pattern sourceBodyPa = Pattern
			.compile("(来源于?|出处|稿源|来自于)[：:][\\s 　]*(?<source>[^\\s|/ 　：:）)”]{1,30})([\\s| 　）)”]|$)");
	private static Pattern pubtimePa = Pattern.compile(
			"(^|[\\s：:| 　])(?<year>(20|19)?\\d{2})[.年/-](?<month>0?[1-9]|1[0-2])[.月/-](?<day>0?[1-9]|[12]\\d|3[01])日?([\\s 　]{0,4}(?<hour>0?\\d|1\\d|2[0-3]):(?<minute>[0-5]?\\d)(:(?<second>[0-5]?\\d))?)?([\\s| 　]|$)");

	public static void extractArticle(Page page, com.cmcc.wltx.collector.spider.model.Article article) {
		String content = article.getContent();
		Date createDate = article.getCreateDate();
		String source = article.getPubSource();
		Set<String> tags = article.getTags();
		String summary = article.getSummary();
		if (null != content && content.length() != 0 && null != source && source.length() != 0 && null != tags
				&& tags.size() != 0 && null != summary && summary.length() != 0 && null != createDate) {
			return;
		}
		org.jsoup.nodes.Document dom = Jsoup.parse(page.getRawText());

		// 正文
		if (article.getSourceType() == Article.RECTYPE_PAPER) {
			Element ozoom = dom.getElementById("ozoom");
			if (null != ozoom) {
				content = ozoom.text().trim();
				if (content.length() != 0) {
					article.setContent(content);
				}
			}
		}
		if (null == content || content.length() == 0) {
			Elements ps = dom.select("p");
			int size = ps.size();
			if (size != 0) {
				List<Element> parents = new ArrayList<Element>();
				List<Integer> counts = new ArrayList<Integer>();
				for (int i = 0; i < size; i++) {
					Element p = ps.get(i);
					Element parent = p.parent();
					boolean exist = false;
					for (int j = 0; j < parents.size(); j++) {
						Element existParent = parents.get(j);
						if (existParent == parent) {
							Integer integer = counts.get(j);
							counts.set(j, integer + 1);
							exist = true;
							break;
						}
					}
					if (!exist) {
						parents.add(parent);
						counts.add(1);
					}
				}

				int maxCountIndex = 0;
				int maxCount = 0;
				int countSize = counts.size();
				for (int i = 0; i < countSize; i++) {
					Integer count = counts.get(i);
					if (count > maxCount) {
						maxCount = count;
						maxCountIndex = i;
					}
				}
				article.setContent(parents.get(maxCountIndex).text());
			}
		}

		// 发布时间
		if (null == createDate) {
			Element pubtime = null;
			Elements pubtimes = dom.select("#pubtime_baidu");
			if (0 != pubtimes.size()) {
				pubtime = pubtimes.get(0);
			} else {
				pubtimes = dom.getElementsMatchingOwnText(pubtimePa);
				if (0 != pubtimes.size()) {
					pubtime = pubtimes.get(0);
				}
			}
			if (null != pubtime) {
				String text = pubtime.text();
				Matcher ma = pubtimePa.matcher(text);
				if (ma.find()) {
					String year = ma.group("year");
					if (year.length() == 2) {
						char tens = year.charAt(0);
						if (tens > '6') {
							year = "19" + year;
						} else {
							year = "20" + year;
						}
					}
					String month = ma.group("month");
					if (month.length() == 1) {
						month = '0' + month;
					}
					String day = ma.group("day");
					if (day.length() == 1) {
						day = '0' + day;
					}

					String hour = ma.group("hour");
					if (null == hour) {
						hour = "00";
					} else if (hour.length() == 1) {
						hour = '0' + hour;
					}
					String minute = ma.group("minute");
					if (null == minute) {
						minute = "00";
					} else if (minute.length() == 1) {
						minute = '0' + minute;
					}
					String second = ma.group("second");
					if (null == second) {
						second = "00";
					} else if (second.length() == 1) {
						second = '0' + second;
					}
					try {
						article.setCreateDate(new SimpleDateFormat("yyyyMMddHHmmss")
								.parse(year + month + day + hour + minute + second));
					} catch (ParseException e) {
						logger.warn("pubtime parse exception", e);
					}
				}
			}
		}

		// 来源
		if ((null == source || source.length() == 0) && null != sourceHeadPa && null != sourceBodyPa) {
			Elements source_baidu = dom.select("#source_baidu");
			if (0 != source_baidu.size()) {
				source = source_baidu.get(0).text();
				if (null != source && (source = source.trim()).length() != 0) {
					article.setPubSource(source);
				}
			} else {
				Elements sourceEles = dom.getElementsMatchingOwnText(sourceHeadPa);
				int size = sourceEles.size();
				for (int i = 0; i < size; i++) {
					String text = sourceEles.get(i).text();
					Matcher sourceMa = sourceBodyPa.matcher(text);
					if (sourceMa.find()) {
						article.setPubSource(sourceMa.group("source"));
						break;
					}
				}
			}
		}

		// 关键词
		if (null == tags || tags.size() == 0) {
			Elements select = dom.select("meta[name=keywords]");
			if (select.size() == 0) {
				select = dom.select("meta[http-equiv=keywords]");
			}
			if (select.size() != 0) {
				String attr = select.get(0).attr("content").trim();
				if (null != attr && attr.length() != 0) {
					if (null == tags) {
						tags = new HashSet<String>();
						article.setTags(tags);
					}
					tags.add(attr);
				}
			}
		}

		// 摘要
		if (null == summary || summary.length() == 0) {
			Elements select = dom.select("meta[name=description]");
			if (select.size() == 0) {
				select = dom.select("meta[http-equiv=description]");
			}
			if (select.size() != 0) {
				String attr = select.get(0).attr("content").trim();
				if (null != attr && attr.length() != 0) {
					article.setSummary(attr);
				}
			}
		}
	}

	public static void extractShangJiArticle(Page page, com.cmcc.wltx.collector.spider.model.Article article) {
		String content = article.getContent();
		if (null != content && content.length() != 0) {
			return;
		}
		org.jsoup.nodes.Document dom = Jsoup.parse(page.getRawText());

		// 正文
		if (null == content || content.length() == 0) {
			Elements ps = dom.select("p");
			int size = ps.size();
			if (size != 0) {
				List<Element> parents = new ArrayList<Element>();
				List<Integer> counts = new ArrayList<Integer>();
				for (int i = 0; i < size; i++) {
					Element p = ps.get(i);
					Element parent = p.parent();
					boolean exist = false;
					for (int j = 0; j < parents.size(); j++) {
						Element existParent = parents.get(j);
						if (existParent == parent) {
							Integer integer = counts.get(j);
							counts.set(j, integer + 1);
							exist = true;
							break;
						}
					}
					if (!exist) {
						parents.add(parent);
						counts.add(1);
					}
				}

				int maxCountIndex = 0;
				int maxCount = 0;
				int countSize = counts.size();
				for (int i = 0; i < countSize; i++) {
					Integer count = counts.get(i);
					if (count > maxCount) {
						maxCount = count;
						maxCountIndex = i;
					}
				}
				article.setContent(parents.get(maxCountIndex).text());
			}
		}
	}

	public static void loadProperties(Set<String> names) {
		boolean updateIgnoreKeywordsRegex = false;
		boolean updateSourceExtractorRegex = false;
		boolean updatePubtimeExtractorRegex = false;
		boolean updateUncorrelatedWords = false;
		boolean updateUncorrelatedNames = false;
		boolean updateTitleIgnoreRegexs = false;
		StringBuilder sql = new StringBuilder("SELECT * FROM t_properties");
		if (null != names && names.size() != 0) {
			sql.append(" where c_name in (");
			for (String name : names) {
				if (null == name || name.length() == 0) {
					continue;
				}
				if (name.startsWith("regex.keywords.ignore.")) {
					if (updateIgnoreKeywordsRegex) {
						continue;
					} else {
						name = "regex.keywords.ignore.common','regex.keywords.ignore.bbs','regex.keywords.ignore.blog','regex.keywords.ignore.paper";
						updateIgnoreKeywordsRegex = true;
					}
				} else if ("regex.extractor.web.source".equals(name)) {
					updateSourceExtractorRegex = true;
				} else if ("regex.extractor.web.pubtime".equals(name)) {
					updatePubtimeExtractorRegex = true;
				} else if ("array.comma.keywords.ignore.linkname.contain".equals(name)) {
					updateUncorrelatedWords = true;
				} else if ("array.comma.keywords.ignore.linkname.equal".equals(name)) {
					updateUncorrelatedNames = true;
				} else if ("array.regex.ignore.title".equals(name)) {
					updateTitleIgnoreRegexs = true;
				}
				sql.append('\'').append(name).append('\'').append(',');
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(")");
		} else {
			updateIgnoreKeywordsRegex = true;
			updateSourceExtractorRegex = true;
			updatePubtimeExtractorRegex = true;
			updateUncorrelatedWords = true;
			updateUncorrelatedNames = true;
			updateTitleIgnoreRegexs = true;
		}

		if (!updateIgnoreKeywordsRegex && !updateSourceExtractorRegex && !updatePubtimeExtractorRegex
				&& !updateUncorrelatedWords && !updateUncorrelatedNames && !updateTitleIgnoreRegexs) {
			return;
		}

		Map<String, String> properties = new HashMap<String, String>();
		Connection conn = MyDataSource.connect();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				properties.put(rs.getString("c_name"), rs.getString("c_value"));
			}
		} catch (SQLException e) {
			throw new Error("properties获取异常", e);
		} finally {
			MyDataSource.release(rs, stmt, conn);
		}

		if (updateIgnoreKeywordsRegex) {
			updateIgnoreKeywordsRegex(properties);
		}
		if (updateSourceExtractorRegex) {
			updateSourceExtractorRegex(properties);
		}
		if (updatePubtimeExtractorRegex) {
			updatePubtimeExtractorRegex(properties);
		}
		if (updateUncorrelatedWords) {
			updateUncorrelatedWords(properties);
		}
		if (updateUncorrelatedNames) {
			updateUncorrelatedNames(properties);
		}
		if (updateTitleIgnoreRegexs) {
			updateTitleIgnoreRegexs(properties);
		}
	}

	private static void updateUncorrelatedNames(Map<String, String> properties) {
		String value = properties.get("array.comma.keywords.ignore.linkname.equal");
		if (null == value || value.length() == 0) {
			throw new RuntimeException("property[array.comma.keywords.ignore.linkname.equal] not found");
		}
		uncorrelatedNames = value.split(",");
		logger.info("load property[array.comma.keywords.ignore.linkname.equal] - {}", value);
	}

	private static void updateUncorrelatedWords(Map<String, String> properties) {
		String value = properties.get("array.comma.keywords.ignore.linkname.contain");
		if (null == value || value.length() == 0) {
			throw new RuntimeException("property[array.comma.keywords.ignore.linkname.contain] not found");
		}
		uncorrelatedWords = value.split(",");
		logger.info("load property[array.comma.keywords.ignore.linkname.contain] - {}", value);
	}

	private static void updatePubtimeExtractorRegex(Map<String, String> properties) {
		String value = properties.get("regex.extractor.web.pubtime");
		if (null == value || value.length() == 0) {
			throw new RuntimeException("property[regex.extractor.web.pubtime] not found");
		}
		pubtimePa = Pattern.compile(value);
		logger.info("load property[regex.extractor.web.pubtime] - {}", value);
	}

	private static void updateSourceExtractorRegex(Map<String, String> properties) {
		String value = properties.get("regex.extractor.web.source");
		if (null == value || value.length() == 0) {
			throw new RuntimeException("property[regex.extractor.web.source] not found");
		}
		String[] split = value.split("\r\n");
		if (split.length != 2) {
			throw new RuntimeException("property[regex.extractor.web.source] error");
		}
		sourceHeadPa = Pattern.compile(split[0]);
		sourceBodyPa = Pattern.compile(split[0] + split[1]);
		logger.info("load property[regex.extractor.web.source] - {}", value);
	}

	private static void updateIgnoreKeywordsRegex(Map<String, String> properties) {
		String rkiCommon = properties.get("regex.keywords.ignore.common");
		if (null == rkiCommon || rkiCommon.length() == 0) {
			throw new RuntimeException("property[regex.keywords.ignore.common] not found");
		}
		String rkiBBS = properties.get("regex.keywords.ignore.bbs");
		if (null == rkiBBS || rkiBBS.length() == 0) {
			throw new RuntimeException("property[regex.keywords.ignore.bbs] not found");
		}
		String rkiBlog = properties.get("regex.keywords.ignore.blog");
		if (null == rkiBlog || rkiBlog.length() == 0) {
			throw new RuntimeException("property[regex.keywords.ignore.blog] not found");
		}
		String rkiPaper = properties.get("regex.keywords.ignore.paper");
		if (null == rkiPaper || rkiPaper.length() == 0) {
			throw new RuntimeException("property[regex.keywords.ignore.paper] not found");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(rkiCommon).append('|');
		sb.append(rkiBBS).append('|');
		sb.append(rkiBlog).append('|');
		sb.append(rkiPaper);
		newsIgnorePatterns = new Pattern[2];
		newsIgnorePatterns[1] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD_ + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT_);
		sb.append("|\\w");
		newsIgnorePatterns[0] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT);
		logger.info("load property[regex.keywords.ignore.common] - {}", rkiCommon);

		sb = new StringBuilder();
		sb.append(rkiCommon).append('|');
		sb.append(rkiBlog).append('|');
		sb.append(rkiPaper);
		bbsIgnorePatterns = new Pattern[2];
		bbsIgnorePatterns[1] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD_ + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT_);
		sb.append("|\\w");
		bbsIgnorePatterns[0] = Pattern.compile(REGEX_KEYWORDS_IGNORE_HEAD + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT);
		logger.info("load property[regex.keywords.ignore.bbs] - {}", rkiBBS);

		sb = new StringBuilder();
		sb.append(rkiCommon).append('|');
		sb.append(rkiBBS).append('|');
		sb.append(rkiPaper);
		blogIgnorePatterns = new Pattern[2];
		blogIgnorePatterns[1] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD_ + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT_);
		sb.append("|\\w");
		blogIgnorePatterns[0] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT);
		logger.info("load property[regex.keywords.ignore.blog] - {}", rkiBlog);

		sb = new StringBuilder();
		sb.append(rkiCommon).append('|');
		sb.append(rkiBBS).append('|');
		sb.append(rkiBlog);
		paperIgnorePatterns = new Pattern[2];
		paperIgnorePatterns[1] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD_ + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT_);
		sb.append("|\\w");
		paperIgnorePatterns[0] = Pattern
				.compile(REGEX_KEYWORDS_IGNORE_HEAD + sb.toString() + REGEX_KEYWORDS_IGNORE_FOOT);
		logger.info("load property[regex.keywords.ignore.paper] - {}", rkiPaper);
	}

	@SuppressWarnings("unchecked")
	public static void updateProperties() throws IOException {
		File file = new File(
				ConstantsHome.USER_DIR + File.separatorChar + "update" + File.separatorChar + "properties");
		if (!file.isFile()) {
			return;
		}
		List<String> lines = FileUtils.readLines(file);
		file.delete();
		if (null == lines || lines.size() == 0) {
			return;
		}
		loadProperties(new HashSet<String>(lines));
	}

	private static void updateTitleIgnoreRegexs(Map<String, String> properties) {
		String value = properties.get("array.regex.ignore.title");
		if (null == value || value.length() == 0) {
			invalidTitlePatterns = null;
		} else {
			String[] split = value.split("\r?\n");
			invalidTitlePatterns = new Pattern[split.length];
			for (int i = 0; i < split.length; i++) {
				invalidTitlePatterns[i] = Pattern.compile(split[i]);
			}
		}
		logger.info("load property[array.regex.ignore.title] - {}", value);
	}

	private static Pattern[] invalidTitlePatterns = { Pattern.compile("(.*\\D|^)404(\\D.*|$)") };

	public static boolean isInvalidTitle(String title) {
		if (null == title || title.length() == 0) {
			return true;
		}
		if (null == invalidTitlePatterns) {
			return false;
		}
		for (Pattern pa : invalidTitlePatterns) {
			if (pa.matcher(title).matches()) {
				return true;
			}
		}
		return false;
	}

	public static Date getDateByStr(String paramTime) {
		Date resultData = null;
		if (StringUtils.isNotBlank(paramTime)) {
			Matcher ma = pubtimePa.matcher(paramTime);
			if (ma.find()) {
				String year = ma.group("year");
				if (year.length() == 2) {
					char tens = year.charAt(0);
					if (tens > '6') {
						year = "19" + year;
					} else {
						year = "20" + year;
					}
				}
				String month = ma.group("month");
				if (month.length() == 1) {
					month = '0' + month;
				}
				String day = ma.group("day");
				if (day.length() == 1) {
					day = '0' + day;
				}

				String hour = ma.group("hour");
				if (null == hour) {
					hour = "00";
				} else if (hour.length() == 1) {
					hour = '0' + hour;
				}
				String minute = ma.group("minute");
				if (null == minute) {
					minute = "00";
				} else if (minute.length() == 1) {
					minute = '0' + minute;
				}
				String second = ma.group("second");
				if (null == second) {
					second = "00";
				} else if (second.length() == 1) {
					second = '0' + second;
				}
				try {
					resultData = new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(year + month + day + hour + minute + second);
				} catch (ParseException e) {
					logger.error("发布时间格式错误", e);
				}
			}
		}
		return resultData;
	}
}
