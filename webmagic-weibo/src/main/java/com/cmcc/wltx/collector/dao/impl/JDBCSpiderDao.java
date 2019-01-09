package com.cmcc.wltx.collector.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cmcc.wltx.collector.dao.SpiderDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.spider.mywebmagic.site.MySite;
import com.cmcc.wltx.database.JedisUtils;

public class JDBCSpiderDao extends JDBCBasicDao implements SpiderDao {

	@Override
	public boolean updateSpiderStatus(String spiderId, int status) throws SQLException {
		if (1 == executeUpdate("update t_spider set c_status = " + status + " where c_id = '" + spiderId + "'")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void loadMySite(MySite site, PreparedStatement stmt, String spiderId)
			throws SQLException, DataAccessException {
		stmt.setString(1, spiderId);
		try (ResultSet rs = stmt.executeQuery()) {
			if (!rs.next()) {
				throw new DataAccessException("spider[" + spiderId + "] not exist");
			}
			// 爬虫名称
			String name = rs.getString("c_name");
			if (null != name && (name = name.trim()).length() != 0) {
				site.setName(name);
			}
			// 爬虫名称
			String ips = rs.getString("c_ip");
			if (null != ips && (ips = ips.trim()).length() != 0) {
				site.setIps(ips);
			}
			// 信源类型
			int recType = rs.getInt("c_type_source");
			if (!rs.wasNull() && recType > 0) {
				site.setRecType(recType);
			}
			// 模板缓存路径
			String templatesPath = rs.getString("c_path_template");
			if (null != templatesPath && (templatesPath = templatesPath.trim()).length() != 0) {
				site.setTemplatesPath(templatesPath);
			}
			// out文件输出路径
			String outPath = rs.getString("c_path_out");
			if (null != outPath && (outPath = outPath.trim()).length() != 0) {
				site.setFilePipelinePath(outPath);
			}
			// 页面编码
			String charset = rs.getString("c_charset");
			if (null != charset && (charset = charset.trim()).length() != 0) {
				site.setCharset(charset);
			}
			// 两次请求间隔时间
			int sleepTime = rs.getInt("c_sleep_request");
			if (!rs.wasNull() && sleepTime >= 0) {
				site.setSleepTime(sleepTime);
			}
			// 重试次数
			int retryTimes = rs.getInt("c_times_retry");
			if (!rs.wasNull() && retryTimes >= 0) {
				site.setRetryTimes(retryTimes);
			}
			// 重试间隔时间
			int retrySleepTime = rs.getInt("c_sleep_retry");
			if (!rs.wasNull() && retrySleepTime >= 0) {
				site.setRetrySleepTime(retrySleepTime);
			}
			// 请求超时时间
			int timeout = rs.getInt("c_timeout");
			if (!rs.wasNull() && timeout > 0) {
				site.setTimeOut(timeout);
			}
			// 两轮采集间隔时间
			int timeinterval = rs.getInt("c_sleep_crawl");
			if (!rs.wasNull() && timeinterval >= 0) {
				site.setTimeInterval(timeinterval);
			}
			// 线程数量
			int threadnum = rs.getInt("c_size_pool_thread");
			if (!rs.wasNull() && threadnum > 0) {
				site.setThreadNum(threadnum);
			}
			// 线程数量
			int taskLimit = rs.getInt("c_limit_task");
			if (!rs.wasNull() && taskLimit > 0) {
				site.setTaskLimit(taskLimit);
			}
			// redis
			String redisHost = rs.getString("c_redis_ip");
			if (null != redisHost && (redisHost = redisHost.trim()).length() != 0) {
				site.setRedisHost(redisHost);
			}
			int redisPort = rs.getInt("c_redis_port");
			if (!rs.wasNull() && redisPort > 0) {
				site.setRedisPort(redisPort);
			}
			String zsetKey = rs.getString("c_redis_key_set_url_target");
			if (null != zsetKey && (zsetKey = zsetKey.trim()).length() != 0) {
				JedisUtils.zsetKey = zsetKey;
			}
			String targetTaskQueueRedisKeyPrefix = rs.getString("c_redis_key_queue_task_target");
			if (null != targetTaskQueueRedisKeyPrefix
					&& (targetTaskQueueRedisKeyPrefix = targetTaskQueueRedisKeyPrefix.trim()).length() != 0) {
				site.setTargetTaskQueueRedisKeyPrefix(targetTaskQueueRedisKeyPrefix);
			}
		}
	}

}
