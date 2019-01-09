package com.cmcc.wltx.collector.logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cmcc.jdbc.MyDataSource;

public class DBSpiderLogger {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DBSpiderLogger.class);

	private Set<String> escapedUrls = new HashSet<String>();
	private final int CACHE_SIZE = 100;
	private List<String> escapedUrlLogCache = new ArrayList<String>(CACHE_SIZE);
	private List<UrlLog> urlLogCache = new ArrayList<UrlLog>(CACHE_SIZE);
	private List<NaviUrlLog> naviUrlLogCache = new ArrayList<NaviUrlLog>(CACHE_SIZE);
	private List<ExtractFailedUrlLog> extractFailedUrlLogCache = new ArrayList<ExtractFailedUrlLog>(CACHE_SIZE);
	private List<ExceptionLog> requestExceptionLogCache = new ArrayList<ExceptionLog>(CACHE_SIZE);
	private List<ArticleLog> articleLogCache = new ArrayList<ArticleLog>(CACHE_SIZE);
	
	private class UrlLog {
		private long ruleTaskId;
		private int index;
		private String url;
		private String name;
		private UrlLog(long ruleTaskId, int index, String url, String name) {
			super();
			this.ruleTaskId = ruleTaskId;
			this.index = index;
			this.url = url;
			this.name = name;
		}
		private long getRuleTaskId() {
			return ruleTaskId;
		}
		private int getIndex() {
			return index;
		}
		private String getUrl() {
			return url;
		}
		private String getName() {
			return name;
		}
	}
	
	private class NaviUrlLog {
		private String url;
		private int warningLevel;
		private NaviUrlLog(String url, int warningLevel) {
			super();
			this.url = url;
			this.warningLevel = warningLevel;
		}
		private String getUrl() {
			return url;
		}
		private int getWarningLevel() {
			return warningLevel;
		}
	}
	
	private class ExtractFailedUrlLog {
		private String url;
		private String fieldName;
		private ExtractFailedUrlLog(String url, String fieldName) {
			super();
			this.url = url;
			this.fieldName = fieldName;
		}
		private String getUrl() {
			return url;
		}
		private String getFieldName() {
			return fieldName;
		}
	}
	private class ExceptionLog {
		private String url;
		private String className;
		private String msg;
		private ExceptionLog(String url, String className, String msg){
			this.url = url;
			this.className = className;
			this.msg = msg;
		}
		private String getUrl() {
			return url;
		}
		private String getClassName() {
			return className;
		}
		private String getMsg() {
			return msg;
		}
		
	}
	
	private class ArticleLog {
		private String url, title, content;
		private ArticleLog(String url, String title, String content) {
			super();
			this.url = url;
			this.title = title;
			this.content = content;
		}
		private String getUrl() {
			return url;
		}
		private String getTitle() {
			return title;
		}
		private String getContent() {
			return content;
		}
	}
	
	private final String spiderId;

	private DBSpiderLogger(String spiderId) {
		super();
		this.spiderId = spiderId;
	}
	
	public void clearEscapedUrls(){
		if (this.escapedUrls.size() == 0) {
			return;
		}
		this.escapedUrls.clear();
	}

	public void logEscapedUrl(String url) {
		synchronized (escapedUrlLogCache) {
			if (escapedUrls.add(url)) {
				escapedUrlLogCache.add(url);
				if (escapedUrlLogCache.size() >= CACHE_SIZE) {
					flushEscapedUrl();
				}
			}
		}
	}
	
	public void logUrl(long ruleTaskId, int index, String url, String name) {
		synchronized (urlLogCache) {
			urlLogCache.add(new UrlLog(ruleTaskId, index, url, name));
			if (urlLogCache.size() >= CACHE_SIZE) {
				flushUrl();
			}
		}
	}

	public void logNaviUrl(int warningLevel, String url) {
		synchronized (naviUrlLogCache) {
			naviUrlLogCache.add(new NaviUrlLog(url, warningLevel));
			if (naviUrlLogCache.size() >= CACHE_SIZE) {
				flushNaviUrl();
			}
		}
	}

	public void logExtractFailedUrl(String fieldName, String url) {
		synchronized (extractFailedUrlLogCache) {
			extractFailedUrlLogCache.add(new ExtractFailedUrlLog(url, fieldName));
			if (extractFailedUrlLogCache.size() >= CACHE_SIZE) {
				flushExtractFailedUrl();
			}
		}
	}
	
	public void logRequestException(String url, Throwable e) {
		String className;
		String msg;
		if (null == e) {
			className = msg = "";
		} else {
			className = e.getClass().getName();
			msg = e.getMessage();
		}
		synchronized (requestExceptionLogCache) {
			requestExceptionLogCache.add(new ExceptionLog(url, className, msg));
			if (requestExceptionLogCache.size() >= CACHE_SIZE) {
				flushRequestException();
			}
		}
	}
	
	public void logArticle(String url, String title, String content) {
		if (title.length() > 53) {
			title = title.substring(0, 25) + " … "
					+ title.substring(title.length() - 25);
		}
		if (content.length() > 53) {
			content = content.substring(0, 25) + " … "
					+ content.substring(content.length() - 25);
		}
		synchronized (articleLogCache) {
			articleLogCache.add(new ArticleLog(url, title, content));
			if (articleLogCache.size() >= CACHE_SIZE) {
				flushArticleLog();
			}
		}
	}

	private void flushEscapedUrl() {
		if (escapedUrlLogCache.size() == 0) {
			return;
		}
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn
					.prepareStatement("INSERT INTO t_log_url_escaped VALUES ('"
							+ spiderId + "', ?)");
			for (String escapedUrl : escapedUrlLogCache) {
				stmt.setString(1, escapedUrl);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.warn("flush escaped url failed", e);
		} finally {
			escapedUrlLogCache.clear();
			MyDataSource.release(stmt, conn);
		}
	}
	
	private void flushUrl() {
		if (urlLogCache.size() == 0) {
			return;
		}
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("INSERT INTO t_url_sorted(c_id_task_rule,c_index,c_url,c_name) VALUES (?, ?, ?, ?)");
			for (UrlLog log : urlLogCache) {
				stmt.setLong(1, log.getRuleTaskId());
				stmt.setInt(2, log.getIndex());
				stmt.setString(3, log.getUrl());
				stmt.setString(4, log.getName());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.warn("flush url failed", e);
		} finally {
			urlLogCache.clear();
			MyDataSource.release(stmt, conn);
		}
	}

	private void flushNaviUrl() {
		if (naviUrlLogCache.size() == 0) {
			return;
		}
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("INSERT INTO t_log_url_navi VALUES ('"
					+ spiderId + "', ?, ?)");
			for (NaviUrlLog log : naviUrlLogCache) {
				stmt.setInt(1, log.getWarningLevel());
				stmt.setString(2, log.getUrl());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.warn("flush navi url failed", e);
		} finally {
			naviUrlLogCache.clear();
			MyDataSource.release(stmt, conn);
		}
	}

	private void flushExtractFailedUrl() {
		if (extractFailedUrlLogCache.size() == 0) {
			return;
		}
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn
					.prepareStatement("INSERT INTO t_log_url_extractfailed VALUES ('"
							+ spiderId + "', ?, ?)");
			for (ExtractFailedUrlLog log : extractFailedUrlLogCache) {
				stmt.setString(1, log.getFieldName());
				stmt.setString(2, log.getUrl());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.warn("flush extract failed url failed", e);
		} finally {
			extractFailedUrlLogCache.clear();
			MyDataSource.release(stmt, conn);
		}
	}
	
	private void flushRequestException() {
		if (requestExceptionLogCache.size() == 0) {
			return;
		}
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn
					.prepareStatement("INSERT INTO t_log_url_exception VALUES ('"
							+ spiderId + "', ?, ?, ?)");
			for (ExceptionLog log : requestExceptionLogCache) {
				stmt.setString(1, log.getUrl());
				stmt.setString(2, log.getClassName());
				stmt.setString(3, log.getMsg());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.warn("flush extract request exception failed", e);
		} finally {
			requestExceptionLogCache.clear();
			MyDataSource.release(stmt, conn);
		}
	}
	
	private void flushArticleLog() {
		if (articleLogCache.size() == 0) {
			return;
		}
		Connection conn = MyDataSource.connect();
		PreparedStatement stmt = null;
		try {
			stmt = conn
					.prepareStatement("INSERT INTO t_log_url_article VALUES ('"
							+ spiderId + "', ?, ?, ?)");
			for (ArticleLog log : articleLogCache) {
				stmt.setString(1, log.getUrl());
				stmt.setString(2, log.getTitle());
				stmt.setString(3, log.getContent());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.warn("flush article log failed", e);
		} finally {
			articleLogCache.clear();
			MyDataSource.release(stmt, conn);
		}
	}

	public void flushAll() {
		//漏掉的url
		flushEscapedUrl();
		//0级爬虫分类url详情
		flushUrl();
		//导航url
		flushNaviUrl();
		//提取失败的url
		flushExtractFailedUrl();
		//访问异常
		flushRequestException();
		//提取文章信息（3级）
		flushArticleLog();
	}

	private static DBSpiderLogger singleton;

	public static DBSpiderLogger getInstance(String spiderId) {
		if (null == singleton) {
			singleton = new DBSpiderLogger(spiderId);
		}
		return singleton;
	}
}
