package com.cmcc.wltx.collector.spider.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cmcc.wltx.collector.spider.model.Article;

public class Html2ArticleA {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(Html2ArticleA.class);

	private static String[][] Filters = {
			{ "(?is)<script.*?>.*?</script>", "" },
			{ "(?is)<style.*?>.*?</style>", "" }, { "(?is)<!--.*?-->", "" },
			{ "(?is)</a>", "</a>\n" } };

	private static boolean _appendMode = false;

	public static boolean getAppendMode() {
		return _appendMode;
	}

	public static void setAppendMode(boolean value) {
		_appendMode = value;
	}

	private static int _depth = 6;

	public static int getDepth() {
		return _depth;
	}

	public static void setDepth(int value) {
		_depth = value;
	}

	private static int _limitCount = 180;

	public static int getLimitCount() {
		return _limitCount;
	}

	public static void setLimitCount(int value) {
		_limitCount = value;
	}

	private static int _headEmptyLines = 2;

	private static int _endLimitCharCount = 20;

	private static int _endAreaTag = 15;

	private final static int MAX_AREA = 15;

	private static int countNewLine(String html) {
		int count = 0;
		for (int i = 0; i < html.length(); i++)
			if (html.charAt(i) == '\n')
				count++;

		return count;
	}

	@SuppressWarnings("finally")
	public static String fixBody(String reg, String body) {
		StringBuffer s = new StringBuffer(body.length());
		try {
			Matcher match = Pattern.compile(reg).matcher(body);
			while (match.find()) {
				String str = match.group();
				StringBuffer sb = new StringBuffer();

				for (int i = 0; i < str.length(); i++) {
					if (str.charAt(i) == '\r' || str.charAt(i) == '\n')
						continue;

					sb.append(str.charAt(i));
				}
				match.appendReplacement(s, sb.toString());
			}
			match.appendTail(s);
		} catch (Exception e) {
			logger.error(body);
			e.printStackTrace();
			return null;
		} finally {
			return s.toString();
		}
	}

	private static String preProcess(String htmlText) {
		htmlText = htmlText.replaceAll("(?is)<!DOCTYPE.*?>", "");
		htmlText = htmlText.replaceAll("(?is)<!--.*?-->", "");
		htmlText = htmlText.replaceAll("(?is)<script.*?>.*?</script>", "");
		htmlText = htmlText.replaceAll("(?is)<style.*?>.*?</style>", "");
		htmlText = htmlText.replaceAll("(?is)<.*?>", "");

		return replaceSpecialChar(htmlText);
	}
	
	private static String preProcessToShangJi(String htmlText) {
		htmlText = htmlText.replaceAll("(?is)<!DOCTYPE.*?>", "");
		htmlText = htmlText.replaceAll("(?is)<!--.*?-->", "");
		htmlText = htmlText.replaceAll("(?is)<script.*?>.*?</script>", "");
		htmlText = htmlText.replaceAll("(?is)<style.*?>.*?</style>", "");

		return replaceSpecialChar(htmlText);
	}

	private static String replaceSpecialChar(String content) {
		String text = content.replaceAll("&quot;", "\"");
		text = text.replaceAll("&ldquo;", "“");
		text = text.replaceAll("&rdquo;", "”");
		text = text.replaceAll("&middot;", "·");
		text = text.replaceAll("&#8231;", "·");
		text = text.replaceAll("&#8212;", "——");
		text = text.replaceAll("&#28635;", "濛");
		text = text.replaceAll("&hellip;", "…");
		text = text.replaceAll("&#23301;", "嬅");
		text = text.replaceAll("&#27043;", "榣");
		text = text.replaceAll("&#8226;", "·");
		text = text.replaceAll("&#40;", "(");
		text = text.replaceAll("&#41;", ")");
		text = text.replaceAll("&#183;", "·");
		text = text.replaceAll("&amp;", "&");
		text = text.replaceAll("&bull;", "·");
		text = text.replaceAll("&lt;", "<");
		text = text.replaceAll("&#60;", "<");
		text = text.replaceAll("&gt;", ">");
		text = text.replaceAll("&#62;", ">");
		text = text.replaceAll("&nbsp;", " ");
		text = text.replaceAll("&#160;", " ");
		text = text.replaceAll("&tilde;", "~");
		text = text.replaceAll("&mdash;", "—");
		text = text.replaceAll("&copy;", "@");
		text = text.replaceAll("&#169;", "@");
		text = text.replaceAll("♂", "");
		text = text.replaceAll("\r\n|\r", "\n");

		return text;
	}

	public static boolean isNavigatePage(String text) {
		if (text == null || text.isEmpty())
			return true;
		String textFilter = "[，。？,.！]";
		int len = text.split(textFilter).length;
		return len < 5;
	}

	public static void GetArticle(String html, Article article) {
		if (countNewLine(html) < 10) {
			html = html.replace(">", ">\n");
		}

		html = java.util.regex.Matcher.quoteReplacement(html);

		String body = "";
		String bodyFilter = "(?is)<body.*?</body>";
		Matcher m = Pattern.compile(bodyFilter).matcher(html);
		if (m.find())
			body = m.group();

		for (String[] filter : Filters)
			body = body.replaceAll(filter[0], filter[1]);

		body = fixBody("(<[^<>]+)\\s*\\n\\s*", body);
		body = preProcess(body);

		// 优先使用从导航页的新闻列表抽取的标题
		String listTitle = article.getListTitle();
		if (null != listTitle && !listTitle.isEmpty()) {
			article.setTitle(listTitle);
		} else {
			article.setTitle(GetTitle(html));
		}
//		article.setCreateDate(GetPublishDate(body));
		article.setContent(GetContent(body));
//		if (isNavigatePage(article.getContent()))
//			article.setContent(null);
//		article.setSource(GetSource(body));
	}
	
	public static void GetShangJiArticle(String html, Article article) {
		if (countNewLine(html) < 10) {
			html = html.replace(">", ">\n");
		}

		html = java.util.regex.Matcher.quoteReplacement(html);

		String body = "";
		String bodyFilter = "(?is)<body.*?</body>";
		Matcher m = Pattern.compile(bodyFilter).matcher(html);
		if (m.find())
			body = m.group();

		for (String[] filter : Filters)
			body = body.replaceAll(filter[0], filter[1]);

		body = fixBody("(<[^<>]+)\\s*\\n\\s*", body);
		body = preProcessToShangJi(body);
		article.setContent(body);
	}

	private static String GetTitle(String html) {
		String titleFilter = "<title>[\\s\\S]*?</title>";
		String h1Filter = "<h[1234].*?>.*?</h[1234]>";
		String clearFilter = "<.*?>";
		String title = "";

		Matcher m = Pattern.compile(titleFilter, Pattern.CASE_INSENSITIVE)
				.matcher(html);
		if (m.find())
			title = m.group(0).replaceAll(clearFilter, "");

		Matcher m1 = Pattern.compile(h1Filter, Pattern.CASE_INSENSITIVE)
				.matcher(html);
		if (m1.find()) {
			String h1 = m1.group(0).replaceAll(clearFilter, "");
			if (!(h1 == null || h1.isEmpty()) && title.startsWith(h1))
				title = h1;
		}

		return title.trim();
	}

	private static String GetSrcAlternative(String body) {
		String html = body.replaceAll("\r", "");
		String srcReg = "\\s*来源\\s*：\n?\\s*.*\\s*\n?";
		Matcher m = Pattern.compile(srcReg, Pattern.CASE_INSENSITIVE).matcher(
				html);
		String src = "";
		if (m.find())
			src = m.group().split("：")[1].trim();
		return (src.length() < MAX_AREA ? src : "");
	}

	private static String GetSource(String bodyText) {
		String[] orgLines = null;
		String[] lines = null;

		orgLines = bodyText.split("\n");
		lines = new String[orgLines.length];

		for (int i = 0; i < orgLines.length; i++) {
			String lineInfo = orgLines[i];
			lineInfo = lineInfo.replaceAll("(?is)</p>|<br.*?/>", "[crlf]");
			lines[i] = lineInfo.replaceAll("(?is)<.*?>", "").trim();
		}

		String newsSrc = "", newsSource = "";
		for (int i = 0; i < lines.length - _endAreaTag; i++) {
			if (lines[i].contains("来源")) {
				String regx = "\\s*来源\\s*：\\s*(.*)\\s*";
				Matcher m = Pattern.compile(regx, Pattern.CASE_INSENSITIVE)
						.matcher(lines[i]);
				if (m.find()) {
					if (m.group(1).contains("&nbsp;"))
						return (m.group(1).split("&nbsp;")[0].length() < MAX_AREA ? m
								.group(1).split("&nbsp;")[0] : "");
					else
						return (m.group(1).length() < MAX_AREA ? m.group(1)
								: "");
				} else {
					newsSource = lines[i];
					int j = 1;
					for (j = 1; j < _endAreaTag; j++) {
						if (newsSource != newsSource + lines[i + j]) {
							newsSrc = lines[i + j];
							return (newsSrc.length() < MAX_AREA ? newsSrc : "");
						}
						newsSource = newsSource + lines[i + j];
					}
				}
			}
		}

		return (newsSrc.length() < MAX_AREA ? newsSrc : "");
	}

	private static Date GetPublishDate(String html) {
		// 2015-11-13 09:56:00
		// 2015年11月13日09:40 2015年11月11日 18:22
		String dateRegNormal = "(\\d{2,4})(-|/)\\d{1,2}(-|/)\\d{1,4}((\\s*)?\\d{2}:\\d{2})?(:\\d{2})?";
		String dateRegYear = "(\\d{2,4})(年)\\d{1,2}(月)\\d{1,4}(日)((\\s*)?\\d{2}:\\d{2})?(:\\d{2})?";
		// +
		// "|((\\d{2,4}年\\d{1,2}月\\d{1,2}日)((\\s*)?\\d{2}:\\d{2})?(:\\d{2})?)";
		String text = html.replace("(?is)<.*?>", "");
		String dateStr = "", dateStrYear = "";
		int start = Integer.MAX_VALUE, startYear = Integer.MAX_VALUE;
		Date result = new Date(1);
		Matcher mYear = Pattern.compile(dateRegYear, Pattern.CASE_INSENSITIVE)
				.matcher(text);
		if (mYear.find()) {
			dateStrYear = mYear.group().trim();
			startYear = mYear.start();
			if (dateStrYear.contains("年")) {
				dateStrYear = dateStrYear.replace("年", "-");
				dateStrYear = dateStrYear.replace("月", "-");
				dateStrYear = dateStrYear.replace("日", " ");
			}
			if (mYear.groupCount() > 1 && mYear.group(1).length() == 2)
				dateStrYear = "20" + dateStrYear; // 年数为两位,则补全
		}
		Matcher m = Pattern.compile(dateRegNormal, Pattern.CASE_INSENSITIVE)
				.matcher(text);
		if (m.find()) {
			try {
				dateStr = m.group().trim();
				start = m.start();
				if (dateStr.contains("/"))
					dateStr = dateStr.replace('/', '-');
				if (m.groupCount() > 1 && m.group(1).length() == 2)
					dateStr = "20" + dateStr; // 年数为两位,则补全
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (start > startYear)
			dateStr = dateStrYear;
		if (dateStr.isEmpty())
			return result;
		SimpleDateFormat formatter_S = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat formatter_M = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat formatter_Y = new SimpleDateFormat("yyyy-MM-dd");

		try {
			result = formatter_S.parse(dateStr);
		} catch (Exception e) {
			// e.printStackTrace();
			try {
				result = formatter_M.parse(dateStr);
			} catch (Exception ex) {
				// ex.printStackTrace();
				try {
					result = formatter_Y.parse(dateStr);
				} catch (Exception exx) {
					// exx.printStackTrace();
				}
			}
		}
		return result;
	}

	private static String GetContent(String bodyText /* String contentWithTags */) {
		String[] orgLines = null;
		String[] lines = null;
		String content = null;
		orgLines = bodyText.split("\n");
		lines = new String[orgLines.length];

		for (int i = 0; i < orgLines.length; i++) {
			String lineInfo = orgLines[i];
			lineInfo = lineInfo.replaceAll("(?is)</p>|<br.*?/>", "[crlf]");
			lines[i] = lineInfo.replaceAll("(?is)<.*?>", "").trim();
		}

		StringBuilder sb = new StringBuilder();
		StringBuilder orgSb = new StringBuilder();

		int preTextLen = 0;
		int startPos = -1;
		for (int i = 0; i < lines.length - _depth; i++) {
			int len = 0;
			for (int j = 0; j < _depth; j++) {
				len += lines[i + j].length();
			}

			if (startPos == -1) {
				if (preTextLen > _limitCount && len > 0) {
					int emptyCount = 0;
					for (int j = i - 1; j > 0; j--) {
						if (lines[j] == null || lines[j].isEmpty()) {
							emptyCount++;
						} else {
							emptyCount = 0;
						}
						if (emptyCount == _headEmptyLines) {
							startPos = j + _headEmptyLines;
							break;
						}
					}

					if (startPos == -1) {
						startPos = i;
					}

					for (int j = startPos; j <= i; j++) {
						sb.append(lines[j]);
						orgSb.append(orgLines[j]);
					}
				}
			} else {
				if (len <= _endLimitCharCount
						&& preTextLen < _endLimitCharCount) {
					if (!_appendMode) {
						break;
					}
					startPos = -1;
				}
				sb.append(lines[i]);
				orgSb.append(orgLines[i]);
			}
			preTextLen = len;
		}

		String result = sb.toString();

		content = result
				.replace("[crlf]", System.getProperty("line.separator"));
		content = content.replaceAll("\r\n  ", "\r\n");

		return content.trim();
	}

}
