package com.cmcc.wltx.collector.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;

import com.cmcc.jdbc.DataSourceException;
import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.dao.DaoFactory;
import com.cmcc.wltx.collector.dao.HomeWeiboSpiderTaskDao;
import com.cmcc.wltx.collector.dao.WeiboUserDao;
import com.cmcc.wltx.collector.exception.ServiceException;
import com.cmcc.wltx.collector.model.HomeWeiboSpiderTask;
import com.cmcc.wltx.collector.model.WeiboUser;
import com.cmcc.wltx.collector.service.HomeWeiboSpiderTaskService;
import com.cmcc.wltx.collector.spider.util.TaskUtils;
import com.cmcc.wltx.database.JedisUtils;

public class RedisHomeWeiboSpiderTaskService implements HomeWeiboSpiderTaskService {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(RedisHomeWeiboSpiderTaskService.class);
	private final String KEY_PREFIX_QUEUE = "queue_task_spider_weibo_home_";
	private volatile boolean neadReleaseLock = true;
	private final String KEY_PREFIX_LOCK = "lock_task_weibo_home_temp";
	private volatile boolean neadReleaseBaseLock = true;
	private final String KEY_PREFIX_LOCK_BASE = "lock_task_weibo_home_base";
	private final HomeWeiboSpiderTaskDao homeWeiboSpiderTaskDao = DaoFactory.getHomeWeiboSpiderTaskDao();
	private final WeiboUserDao weiboUserDao = DaoFactory.getWeiboUserDao();

	// redis单节点
	@Override
	public List<HomeWeiboSpiderTask> tasksForSpider(String spiderId, int limit, double[] weights)
			throws ServiceException {
		try (Jedis jedis = JedisUtils.createJedis()) {
			if (neadReleaseLock) {
				// 新增微博账户任务
				jedis.srem(KEY_PREFIX_LOCK, spiderId);
				neadReleaseLock = false;
			}
			if (neadReleaseBaseLock) {
				// 信息补采任务
				jedis.srem(KEY_PREFIX_LOCK_BASE, spiderId);
				neadReleaseBaseLock = false;
			}

			// 新增加的微博账号
			String key = KEY_PREFIX_QUEUE + "temp";
			int taskTotal = jedis.llen(key).intValue();
			if (taskTotal > 0) {
				jedis.sadd(KEY_PREFIX_LOCK, spiderId);
				neadReleaseLock = true;
				logger.info("lock for new users - {}", taskTotal);
				limit = taskTotal > limit ? limit : taskTotal;
				Pipeline pipe = jedis.pipelined();
				for (int i = 0; i < limit; i++) {
					pipe.rpop(key);
				}
				List<Object> ress = pipe.syncAndReturnAll();
				List<HomeWeiboSpiderTask> tasks = new ArrayList<HomeWeiboSpiderTask>(limit);
				for (Object obj : ress) {
					if (null == obj) {
						break;
					}
					long uid;
					try {
						uid = Long.parseLong((String) obj);
					} catch (NumberFormatException e) {
						throw new Error("uid[" + obj + "]非数字", e);
					}
					tasks.add(new HomeWeiboSpiderTask(uid, HomeWeiboSpiderTask.LEVEL_TEMP));
				}

				int size = tasks.size();
				logger.info("pop " + size + " tasks from level " + HomeWeiboSpiderTask.LEVEL_TEMP);
				if (size > 0) {
					return tasks;
				}
				jedis.srem(KEY_PREFIX_LOCK, spiderId);
				neadReleaseLock = false;
			}

			// 补采基础信息的微博账号
			key = KEY_PREFIX_QUEUE + "base";
			taskTotal = jedis.llen(key).intValue();
			if (taskTotal > 0) {
				jedis.sadd(KEY_PREFIX_LOCK_BASE, spiderId);
				neadReleaseBaseLock = true;
				logger.info("lock for base users - {}", taskTotal);
				limit = taskTotal > limit ? limit : taskTotal;
				Pipeline pipe = jedis.pipelined();
				for (int i = 0; i < limit; i++) {
					pipe.rpop(key);
				}
				List<Object> ress = pipe.syncAndReturnAll();
				List<HomeWeiboSpiderTask> tasks = new ArrayList<HomeWeiboSpiderTask>(limit);
				Pattern taskPa = Pattern.compile("(\\d+)_(\\d+)");
				for (Object obj : ress) {
					if (null == obj) {
						break;
					}
					Matcher taskMa = taskPa.matcher((String) obj);
					if (!taskMa.matches()) {
						logger.warn("基础信息补采任务格式错误 - {}", obj.toString());
						continue;
					}
					int level = Integer.parseInt(taskMa.group(1)) * -1;
					long uid = Long.parseLong(taskMa.group(2));
					tasks.add(new HomeWeiboSpiderTask(uid, level));
				}

				int size = tasks.size();
				logger.info("pop " + size + " tasks from base queue");
				if (size > 0) {
					return tasks;
				}
				jedis.srem(KEY_PREFIX_LOCK_BASE, spiderId);
				neadReleaseBaseLock = false;
			}

			double[] taskTotals = { jedis.llen(KEY_PREFIX_QUEUE + 1), jedis.llen(KEY_PREFIX_QUEUE + 2),
					jedis.llen(KEY_PREFIX_QUEUE + 3), jedis.llen(KEY_PREFIX_QUEUE + 4),
					jedis.llen(KEY_PREFIX_QUEUE + 5) };
			// double[] weights = {1, 3, 30, 120, 1440};原始
			// double[] weights = { 1, 6, 120, 360, 480 };// 新频率
			int[] pops = TaskUtils.countPops(limit, taskTotals, weights);
			return popTasks(jedis, limit, pops);
		}
	}

	private List<HomeWeiboSpiderTask> popTasks(Jedis jedis, int limit, int[] pops) {
		List<HomeWeiboSpiderTask> tasks = new ArrayList<HomeWeiboSpiderTask>(limit);
		for (int i = 0; i < pops.length; i++) {
			if (pops[i] == 0) {
				continue;
			}

			int taskLevel = i + 1;
			logger.info("pop " + pops[i] + " tasks from level " + taskLevel);
			String key = KEY_PREFIX_QUEUE + taskLevel;
			Pipeline pipe = jedis.pipelined();
			for (int j = 0; j < pops[i]; j++) {
				pipe.rpoplpush(key, key);
			}
			List<Object> ress = pipe.syncAndReturnAll();
			for (Object res : ress) {
				if (null == res) {
					// 调级改队列名可能造成此处获取到null
					continue;
				}
				long uid;
				try {
					uid = Long.parseLong((String) res);
				} catch (NumberFormatException e) {
					throw new Error("uid[" + res + "]非数字", e);
				}
				tasks.add(new HomeWeiboSpiderTask(uid, taskLevel));
			}
		}
		return tasks;
	}

	// redis集群
	@Override
	public List<HomeWeiboSpiderTask> tasksForSpiderNew(String spiderId, int limit) throws ServiceException {
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = JedisUtils.jc;
		} catch (Exception e) {
			logger.error("tasksForSpiderNew the JedisCluster find failed - ", e);
		}
		if (null != jedisCluster) {
			try {
				if (neadReleaseLock) {
					// 新增微博账户任务
					jedisCluster.srem(KEY_PREFIX_LOCK, spiderId);
					neadReleaseLock = false;
				}

				// 新增加的微博账号
				String key = KEY_PREFIX_QUEUE + "temp";
				int taskTotal = jedisCluster.llen(key).intValue();
				if (taskTotal > 0) {
					jedisCluster.sadd(KEY_PREFIX_LOCK, spiderId);
					neadReleaseLock = true;
					logger.info("lock for new users - {}", taskTotal);
					limit = taskTotal > limit ? limit : taskTotal;
					List<String> ress = new ArrayList<>();
					for (int i = 0; i < limit; i++) {
						String wid = jedisCluster.rpop(key);
						if (StringUtils.isNotBlank(wid)) {
							ress.add(wid);
						}
					}
					List<HomeWeiboSpiderTask> tasks = new ArrayList<HomeWeiboSpiderTask>(ress.size());
					for (String obj : ress) {
						long uid;
						try {
							uid = Long.parseLong(obj);
						} catch (NumberFormatException e) {
							throw new Error("uid[" + obj + "]非数字", e);
						}
						tasks.add(new HomeWeiboSpiderTask(uid, HomeWeiboSpiderTask.LEVEL_TEMP));
					}

					int size = tasks.size();
					logger.info("pop " + size + " tasks from level " + HomeWeiboSpiderTask.LEVEL_TEMP);
					if (size > 0) {
						return tasks;
					}
					jedisCluster.srem(KEY_PREFIX_LOCK, spiderId);
					neadReleaseLock = false;
				}

				double[] taskTotals = { jedisCluster.llen(KEY_PREFIX_QUEUE + 1),
						jedisCluster.llen(KEY_PREFIX_QUEUE + 2), jedisCluster.llen(KEY_PREFIX_QUEUE + 3),
						jedisCluster.llen(KEY_PREFIX_QUEUE + 4), jedisCluster.llen(KEY_PREFIX_QUEUE + 5) };
				// double[] weights = { 1, 6, 120, 360, 480 };
				double[] weights = { 120, 120, 120, 360, 480 };// 新频率
				int[] pops = TaskUtils.countPops(limit, taskTotals, weights);
				return popTasksNew(jedisCluster, limit, pops);
			} catch (Exception e) {
				throw new ServiceException(e);
			}
		}
		return new ArrayList<HomeWeiboSpiderTask>();
	}

	private List<HomeWeiboSpiderTask> popTasksNew(JedisCluster jedisCluster, int limit, int[] pops) {
		List<HomeWeiboSpiderTask> tasks = new ArrayList<HomeWeiboSpiderTask>(limit);
		for (int i = 0; i < pops.length; i++) {
			if (pops[i] == 0) {
				continue;
			}

			int taskLevel = i + 1;
			logger.info("pop " + pops[i] + " tasks from level " + taskLevel);
			String key = KEY_PREFIX_QUEUE + taskLevel;
			List<String> ress = new ArrayList<>();
			for (int j = 0; j < pops[i]; j++) {
				ress.add(jedisCluster.rpoplpush(key, key));
			}
			for (String res : ress) {
				if (null == res) {
					// 调级改队列名可能造成此处获取到null
					continue;
				}
				long uid;
				try {
					uid = Long.parseLong(res);
				} catch (NumberFormatException e) {
					throw new Error("uid[" + res + "]非数字", e);
				}
				tasks.add(new HomeWeiboSpiderTask(uid, taskLevel));
			}
		}
		return tasks;
	}

	@Override
	public void releaseTasks(String spiderId) throws ServiceException {
	}

	// redis单节点变更为集群
	@Override
	public void transferLevel_old(WeiboUser user, int from, int to) {
		if (from == to) {
			return;
		}
		JedisCluster jedisCluster = null;
		try {
			jedisCluster = JedisUtils.jc;
		} catch (Exception e) {
			logger.error("transferLevel the JedisCluster find failed - ", e);
		}
		if (null != jedisCluster) {
			Long id = user.getId();
			String uid = id.toString();
			logger.info("uid[{}] transfer[{}>{}] start", uid, from, to);
			Connection conn = MyDataSource.getCurrentConnection();
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				String msg = "caught SQLException[" + e.getErrorCode() + "," + e.getSQLState()
						+ "] when set auto commit false";
				logger.error(msg, e);
				MyDataSource.releaseCurrentConnection();
				throw new DataSourceException(msg, e);
			}
			try {
				if (from == HomeWeiboSpiderTask.LEVEL_TEMP) {
					logger.info("transfer level - update {}", user);
					weiboUserDao.update(user, null);
				} else {
					logger.info("transfer level - delete {} from table_{}", uid, from);
					if (0 == homeWeiboSpiderTaskDao.delete(new HomeWeiboSpiderTask(id, from))) {
						logger.warn("id[{}] not in table_{}", uid, from);
						conn.rollback();
						return;
					}
				}
				logger.info("transfer level - insert {} into table_{}", uid, to);
				homeWeiboSpiderTaskDao.create(new HomeWeiboSpiderTask(id, to));
				String key_from = KEY_PREFIX_QUEUE + from;
				String key_to = KEY_PREFIX_QUEUE + to;
				if (null != jedisCluster) {
					if (from != HomeWeiboSpiderTask.LEVEL_TEMP) {
						logger.info("transfer level - lrem {} from {}", uid, key_from);
						if (1 != jedisCluster.lrem(key_from, 1, uid)) {
							logger.warn("uid[{}] not in {}", uid, key_from);
							conn.rollback();
							return;
						}
					}
					if (jedisCluster.exists(key_to + "_temp")) {
						key_to = key_to + "_temp_orary";
					}
					logger.info("transfer level - lpush {} into {}", uid, key_to);
					jedisCluster.lpush(key_to, uid);
				}

				try {
					conn.commit();
					logger.info("uid[{}] transfer[{}>{}] succeed", uid, from, to);
				} catch (SQLException e) {
					logger.error("caught SQLException[" + e.getErrorCode() + "," + e.getSQLState()
							+ "] when commit, start rollback redis", e);
					if (null != jedisCluster) {
						logger.info("transfer level - lrem {} from {}", uid, key_to);
						if (1 != jedisCluster.lrem(key_to, 1, uid)) {
							logger.error("uid[{}] not in {}", uid, key_to);
						}
						if (from != HomeWeiboSpiderTask.LEVEL_TEMP) {
							logger.info("transfer level - lpush {} into {}", uid, key_from);
							jedisCluster.lpush(key_from, uid);
						}
					}
					throw e;
				}
			} catch (Throwable t) {
				logger.error("uid[" + uid + "] transfer[" + from + ">" + to + "] failed", t);
				try {
					conn.rollback();
				} catch (SQLException e) {
					logger.error("caught SQLException[" + e.getErrorCode() + "," + e.getSQLState() + "] when rollback",
							e);
				}
				throw new Error(t);
			} finally {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					String msg = "caught SQLException[" + e.getErrorCode() + "," + e.getSQLState()
							+ "] when set auto commit true";
					logger.error(msg, e);
					throw new DataSourceException(msg, e);
				} finally {
					MyDataSource.releaseCurrentConnection();
				}
			}
		}
	}

	// redis单节点
	@Override
	public void transferLevel(WeiboUser user, int from, int to) {
		if (from == to) {
			return;
		}

		Long id = user.getId();
		String uid = id.toString();
		logger.info("uid[{}] transfer[{}>{}] start", uid, from, to);
		Connection conn = MyDataSource.getCurrentConnection();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			String msg = "caught SQLException[" + e.getErrorCode() + "," + e.getSQLState()
					+ "] when set auto commit false";
			logger.error(msg, e);
			MyDataSource.releaseCurrentConnection();
			throw new DataSourceException(msg, e);
		}
		try {
			if (from == HomeWeiboSpiderTask.LEVEL_TEMP) {
				logger.info("transfer level - update {}", user);
				weiboUserDao.update(user, null);
			} else {
				logger.info("transfer level - delete {} from table_{}", uid, from);
				if (0 == homeWeiboSpiderTaskDao.delete(new HomeWeiboSpiderTask(id, from))) {
					logger.warn("id[{}] not in table_{}", uid, from);
					conn.rollback();
					return;
				}
			}
			logger.info("transfer level - insert {} into table_{}", uid, to);
			homeWeiboSpiderTaskDao.create(new HomeWeiboSpiderTask(id, to));
			String key_from = KEY_PREFIX_QUEUE + from;
			String key_to = KEY_PREFIX_QUEUE + to;
			try (Jedis jedis = JedisUtils.createJedis()) {
				if (from != HomeWeiboSpiderTask.LEVEL_TEMP) {
					logger.info("transfer level - lrem {} from {}", uid, key_from);
					if (1 != jedis.lrem(key_from, 1, uid)) {
						logger.warn("uid[{}] not in {}", uid, key_from);
						conn.rollback();
						return;
					}
				}
				if (jedis.exists(key_to + "_temp")) {
					key_to = key_to + "_temp_orary";
				}
				logger.info("transfer level - lpush {} into {}", uid, key_to);
				jedis.lpush(key_to, uid);
			}

			try {
				conn.commit();
				logger.info("uid[{}] transfer[{}>{}] succeed", uid, from, to);
			} catch (SQLException e) {
				logger.error("caught SQLException[" + e.getErrorCode() + "," + e.getSQLState()
						+ "] when commit, start rollback redis", e);
				try (Jedis jedis = JedisUtils.createJedis()) {
					logger.info("transfer level - lrem {} from {}", uid, key_to);
					if (1 != jedis.lrem(key_to, 1, uid)) {
						logger.error("uid[{}] not in {}", uid, key_to);
					}
					if (from != HomeWeiboSpiderTask.LEVEL_TEMP) {
						logger.info("transfer level - lpush {} into {}", uid, key_from);
						jedis.lpush(key_from, uid);
					}
				}
				throw e;
			}
		} catch (Throwable t) {
			logger.error("uid[" + uid + "] transfer[" + from + ">" + to + "] failed", t);
			try {
				conn.rollback();
			} catch (SQLException e) {
				logger.error("caught SQLException[" + e.getErrorCode() + "," + e.getSQLState() + "] when rollback", e);
			}
			throw new Error(t);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				String msg = "caught SQLException[" + e.getErrorCode() + "," + e.getSQLState()
						+ "] when set auto commit true";
				logger.error(msg, e);
				throw new DataSourceException(msg, e);
			} finally {
				MyDataSource.releaseCurrentConnection();
			}
		}
	}
}