package com.cmcc.wltx.collector.pageprocessor.weibo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;

import redis.clients.jedis.Jedis;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.spider.mywebmagic.MySpider;
import com.cmcc.wltx.database.JedisUtils;

public class WeiboHeaderUrlPageProcessor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WeiboHeaderUrlPageProcessor.class);
	private static List<HttpHost> proxys = new ArrayList<HttpHost>();
	private static final String REDIS_KEY_PREFIX_IMGID = "imgId_";

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("缺少爬虫启动参数");
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
		String filePath = spiderIds[0];
		int proxyType = 2;
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
		MySpider.test = true;
		if (MySpider.test) {
			// 初始化redis
			JedisUtils.initPool("127.0.0.1", 6379);
		} else {
			JedisUtils.initPoolBySentinel();
		}
		initProxy(proxyType);
		try {
			do {
				File file = new File(filePath);
				if (!file.isDirectory()) {
					logger.error("该路径所对应的对象不是目录！");
				} else if (file.isDirectory()) {
					String[] filelist = file.list();
					for (int i = 0; i < filelist.length; i++) {
						String mainPath = filePath + "/" + filelist[i];
						File demofile = new File(mainPath);
						if (!demofile.isDirectory()) {
							logger.error("该路径所对应的对象不是目录！");
						} else if (demofile.isDirectory()) {
							InstallUrlLine(mainPath);
						}
					}
				}
				if (2 == proxyType) {
					initProxy(proxyType);
				}
			} while (true);
		} catch (Exception e) {
			logger.error("WeiboHeaderUrlPageProcessor：", e.getMessage());
		} finally {
			JedisUtils.closePool();
		}
	}

	public static void InstallUrlLine(String filePath) {
		try {
			File file = new File(filePath);
			if (!file.isDirectory()) {
				logger.error("InstallUrlLine - 该路径所对应的对象不是目录！");
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					String fileName = filelist[i];
					if (fileName.contains("DONE")) {
						continue;
					}
					File demofile = new File(filePath + "/" + fileName);
					if (demofile.isDirectory()) {
						demofile.delete();
						logger.error("InstallUrlLine - 该路径所对应的对象是目录！");
					} else if (!demofile.isDirectory()) {
						int count = readFileByLines(demofile);
						if (count > 0) {
							String currentFilePath = demofile.getAbsolutePath() + ".DONE";
							File dest = new File(currentFilePath);
							demofile.renameTo(dest);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("InstallUrlLine - ", e);
		}
	}

	/**
	 * @param file
	 * @return
	 */
	public static int readFileByLines(File file) {
		int num = 0;
		List<Map<String, String>> urlList = getUrlLinesFromFile(file);
		if (null != urlList && urlList.size() > 0) {
			for (Map<String, String> urlMap : urlList) {
				downloadHeaderUrl(urlMap);
			}
			num = urlList.size();
		}
		return num;
	}

	public static List<Map<String, String>> getUrlLinesFromFile(File outFile) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile), "UTF-8"));
			String line = null;
			List<Map<String, String>> list = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				if (StringUtils.isEmpty(line)) {
					continue;
				}
				Map<String, String> urlMap = readLine(line);
				if (null != urlMap && urlMap.size() > 0) {
					list.add(urlMap);
				}
			}
			reader.close();
			return list;
		} catch (UnsupportedEncodingException e) {
			logger.error("getUrlLinesFromFile - ", e);
		} catch (FileNotFoundException e) {
			logger.error("getUrlLinesFromFile - ", e);
		} catch (IOException e) {
			logger.error("getUrlLinesFromFile - ", e);
		} catch (Exception e) {
			logger.error("getUrlLinesFromFile - ", e);
		}
		return null;
	}

	public static Map<String, String> readLine(String line) {
		String[] fields = line.split("\t");
		if (fields == null) {
			return null;
		}
		Map<String, String> urlMap = new HashMap<>();
		urlMap.put("createTime", fields[0]);
		urlMap.put("weiboId", fields[1]);
		urlMap.put("nickName", fields[2]);
		urlMap.put("headerUrl", fields[3]);
		return urlMap;
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

	public static String createFile() {
		File outPath = new File(System.getProperty("user.dir") + File.separator + "img");
		if (!outPath.exists() || !outPath.isDirectory()) {
			if (!outPath.mkdirs()) {
				logger.error("创建输出目录失败 - " + outPath.getAbsolutePath());
				return null;
			}
		}
		String dir = outPath.getAbsolutePath();
		if (!dir.endsWith("/") && !dir.endsWith("\\")) {
			dir += File.separator;
		}
		Calendar calendar = Calendar.getInstance();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String time = formatter.format(calendar.getTime());

		// 拼接完整的目录路径，加上日期部分
		String _dir = dir + time;

		// 判断目录是否存在，如不存在则新建目录
		File dirFile = new File(_dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		return dirFile.getAbsolutePath();
	}

	public static void initProxy(int proxyType) {
		logger.info("init proxy - {}", proxyType);
		proxys.clear();
		if (proxyType < 1 || proxyType > 7) {
			return;
		}

		Connection conn = MyDataSource.connect();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			switch (proxyType) {
			case 1:
				rs = stmt.executeQuery("SELECT c_host,c_port FROM t_config_proxy where c_status = 1");
				break;
			case 2:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_status = 2 and c_effectiveness > -80 and c_trend > 9 and c_time_create < "
								+ (System.currentTimeMillis() - 86400000l));
				break;
			case 3:
				break;
			case 4:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_effectiveness = 127 and (c_trend = 127 or c_trend = 0)");
				break;
			case 5:
				break;
			case 6:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_status = 2 and c_effectiveness > -80 and c_trend > 9 and c_time_create < "
								+ (System.currentTimeMillis() - 86400000l));
				break;
			case 7:
				rs = stmt.executeQuery(
						"SELECT c_host,c_port FROM t_config_proxy where c_effectiveness > -80 and (c_trend > 9 or c_trend = 0) and c_time_create < "
								+ (System.currentTimeMillis() - 86400000l));
				break;
			default:
				break;
			}

			Set<String> hosts = new HashSet<String>();
			if (null != rs) {
				while (rs.next()) {
					String host = rs.getString("c_host");
					if (hosts.add(host)) {
						proxys.add(new HttpHost(host, rs.getInt("c_port")));
					}
				}
			}
		} catch (SQLException e) {
			logger.error("代理配置获取异常", e);
		} finally {
			MyDataSource.release(rs, stmt, conn);
		}
	}

	public static void downloadHeaderUrl(Map<String, String> urlMap) {
		if (null == urlMap || urlMap.size() <= 0) {
			logger.error("null == urlMap || urlMap.size() <= 0");
			return;
		}
		String weiboId = urlMap.get("weiboId");
		String nickName = urlMap.get("nickName");
		String headerUrl = urlMap.get("headerUrl");
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
			if (null == jedis) {
				Boolean isExits = JedisUtils.jc.exists(key);
				if (isExits) {// 重复
					logger.info("微博账户：-{}-的头像地址：-{}-已下载；", weiboId, headerUrl);
					return;
				}
				JedisUtils.jc.set(key, nowTime);
			} else {
				Boolean isExits = jedis.exists(key);
				if (isExits) {// 重复
					logger.info("微博账户：-{}-的头像地址：-{}-已下载；", weiboId, headerUrl);
					return;
				}
				jedis.set(key, nowTime);
			}
			String imgName = weiboId + ".jpg";// 以微博id命名
			String savePath = createFile();
			HttpHost proxy = proxys.get(ThreadLocalRandom.current().nextInt(proxys.size()));
			String result = down(headerUrl, imgName, savePath, proxy);
			if ("error".equals(result)) {
				result = down(headerUrl, imgName, savePath, null);
			}
			if ("error".equals(result)) {
				if (null == jedis) {
					JedisUtils.jc.del(key);
				} else {
					jedis.del(key);
				}
				org.slf4j.Logger statisticLogger = org.slf4j.LoggerFactory
						.getLogger("com.cmcc.wltx.collector.statistics.weibo.home.missUrl");
				statisticLogger.info("{}\t{}\t{}\t{}", nowTime, weiboId, nickName, headerUrl);
			}
		} catch (Exception e) {
			logger.error("downloadHeaderUrl下载微博头像", e);
		} finally {
			if (null != jedis) {
				jedis.close();
			}
		}
	}

	private static String down(String urlString, String filename, String savePath, HttpHost proxy) throws Exception {
		String result = "error";
		// 构造URL
		URL url = new URL(urlString);
		URLConnection con = null;
		String hostName;
		int port;
		if (null == proxy) {
			hostName = "";
			port = 0;
			con = url.openConnection();
		} else {
			hostName = proxy.getHostName();
			port = proxy.getPort();
			SocketAddress addr = new InetSocketAddress(hostName, port);
			Proxy typeProxy = new Proxy(Proxy.Type.HTTP, addr);
			// 打开连接
			try {
				con = url.openConnection(typeProxy);
			} catch (Exception e) {
				logger.error("down - ", e);
				hostName = "";
				port = 0;
				con = url.openConnection();
			}
		}
		logger.info("Down header url ~{}~{}~{}", urlString, null == proxy ? "" : hostName + ':' + port,
				System.currentTimeMillis());
		if (con == null) {
			logger.error("URLConnection is null");
			return result;
		}
		// 设置请求超时为5s
		con.setConnectTimeout(5 * 1000);
		// 输入流
		InputStream is = null;
		try {
			is = con.getInputStream();
		} catch (Exception e) {
			logger.error("URLConnection getInputStream() is failed", e);
			return result;
		}

		// 1K的数据缓冲
		byte[] bs = new byte[1024];
		// 读取到的数据长度
		int len;
		// 输出的文件流
		File sf = new File(savePath);
		if (!sf.exists()) {
			sf.mkdirs();
		}
		OutputStream os = new FileOutputStream(sf.getPath() + "/" + filename);
		// 开始读取
		while ((len = is.read(bs)) != -1) {
			os.write(bs, 0, len);
		}
		// 完毕，关闭所有链接
		os.close();
		is.close();
		return "success";
	}
}