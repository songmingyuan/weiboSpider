package com.cmcc.wltx.collector;

import java.io.File;
import java.util.regex.Pattern;

public class ConstantsHome {
	public static final Pattern PATTERN_ARTICLE_POSSIBLE = Pattern.compile(
			"http://(?!(bbs|tv?|blog|games?|search)\\.)(\\w+\\.)+(cn|com(.cn)?)/(?!(bbs|blog|games?|search)/).*(\\w{8,}|\\d{4,}).*");
	public static final Pattern PATTERN_ARTICLE_POSSIBLE_STRICT = Pattern.compile(
			"http://(?!(bbs|tv?|blog|games?|search)\\.)(\\w+\\.)+(cn|com(.cn)?)/(?!(bbs|blog|games?|search)/).*(\\w{16,}|201[0-5]|200\\d|199\\d).*");
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final File BLOCK = new File(ConstantsHome.USER_DIR + File.separatorChar + "block");
	public static final File FINISH = new File(ConstantsHome.USER_DIR + File.separatorChar + "finish");
	public static final File UPDATE = new File(ConstantsHome.USER_DIR + File.separatorChar + "update");
	public static final File UPDATE_PROXY = new File(UPDATE, "proxy");
	public static final String DEFAULT_TEMPLATES_PATH = USER_DIR + File.separator + "templates";
	public static final String REGEX_CONFIG = "configuration.*\\.xml";
	public static final String REQUEST_EXTRA_TYPE = "type";
	public static final String REQUEST_EXTRA_CHANNEL = "channel";
	public static final String REQUEST_EXTRA_PAGENUM = "pageNum";
	public static final String REQUEST_EXTRA_PAGEINIT = "pageInit";
	public static final String REQUEST_EXTRA_ARTICLE = "article";
	public static final String REQUEST_EXTRA_PROXY = "proxy";
	public static final String REQUEST_EXTRA_PROXY_TYPE = "proxy_type";
	public static final String REQUEST_EXTRA_RETRY_NUM = "retry_num";
	public static final String REQUEST_EXTRA_PROXY_INVALID_TIME = "invalidTime";
	public static final String REQUEST_EXTRA_PROXY_CHANGE = "proxyChange";
	public static final String REQUEST_EXTRA_KEYWORD = "keyword";
	public static final String REQUEST_EXTRA_DEEP = "deep";
	public static final String REQUEST_EXTRA_TASK_ID = "taskId";
	public static final String REQUEST_EXTRA_IS_NAVI = "isNavi";
	public static final String REQUEST_EXTRA_WORKED_TIME = "workedTime";
	public static final String REQUEST_EXTRA_LEVEL = "level";
	public static final String REQUEST_EXTRA_TEMPLATE_TYPE = "templateType";
	public static final String REQUEST_EXTRA_TEMPLATE_ID = "templateId";
	public static final String REQUEST_EXTRA_SITE_TASK_ID = "siteTaskId";
	public static final String REQUEST_EXTRA_RULE_URL = "ruleUrl";
	public static final String REQUEST_EXTRA_PAGE_TYPE = "pageType";
	public static final String REQUEST_EXTRA_CITY = "city";
	public static final String REQUEST_EXTRA_PROVINCE = "province";
	public static final String REQUEST_EXTRA_ADDRESS = "address";
	public static final String REQUEST_EXTRA_SHOP = "shop";
	public static final String REQUEST_EXTRA_BUSINESS_TYPE = "businessType";
	public static final String REQUEST_EXTRA_TITLE = "title";
	public static final String REQUEST_EXTRA_PARAMS = "params";
	public static final String REQUEST_EXTRA_RELEASE_TIME = "release_time";
	public static final String REQUEST_EXTRA_NAVI_SINGLE = "navi_single";
	public static final String REQUEST_EXTRA_SOURCE_TYPE = "sourceType";
	public static final String NAME_VALUE_PAIR = "nameValuePair";
	public static final String ENTITY_FORM_UTF8 = "entity_form_utf8";
	public static final String REQUEST_EXTRA_LINK_NAME = "linkName";
	public static final String REQUEST_EXTRA_HEADERS = "requestHeaders";
	public static final String REQUEST_EXTRA_CHARSET = "charsetParam";
	public static final String REQUEST_EXTRA_ENCODE = "encodeParam";
	public static final String REQUEST_EXTRA_URLPREFIX = "urlPrefix";
	public static final String REQUEST_EXTRA_SPECIAL_ID = "specialId";
	public static final String REQUEST_EXTRA_IS_AUTO = "isAuto";
	public static final String REQUEST_HEADER_LASTMODIFIED = "Last-Modified";
	public static final String REQUEST_HEADER_SETCOOKIE = "Set-Cookie";
	public static final String REQUEST_HEADER_COOKIE = "Cookie";
	public static final String CHARSET_DEFAULT = "UTF-8";
	public static final String PREFIX_URL_PROTOCOL_HTTP = "http";
	public static final char SEPARATOR_EXCEL = '\t';
	public static final Pattern PATTERN_CMCCCEL = Pattern.compile("1(3[4-9]|47|5[012789]|78|8[23478])\\d{8}");
	public static final int EFFECTIVE_PERIOD_NAVI = 691200000;
	public static final int EFFECTIVE_PERIOD_TARGET = 172800000;
	public static final int EFFECTIVE_MIN_INTERVAL = 86400000;
	public static final String LOGGER_NAME_ACCIDENT = "accident";
	public static final String CHROME_PC_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko";
}
