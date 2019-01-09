package com.cmcc.wltx.collector.spider.mywebmagic.pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class JsonWriterPipline implements Pipeline {
	private Logger logger = LoggerFactory.getLogger(getClass());

	// 默认路径
	private static final String DETAULT_PATH = System.getProperty("user.dir") + File.separator + "out";
	private String path = null;
	// 每个文件写的数量
	private Integer limitCnt = Integer.valueOf(100);
	private Integer cnt = Integer.valueOf(0);
	// 爬虫类型
	private String spiderType = "default";

	private OutputStreamWriter outputStreamWriter = null;
	private FileOutputStream fileOutputStream = null;
	private BufferedWriter bufferedWriter = null;
	private File file = null;

	public JsonWriterPipline() {
		// 判断是否通过系统变量配置路径
		String webmagicPath = System.getProperty("webmagic.path");
		setPath(webmagicPath != null ? webmagicPath : DETAULT_PATH);
	}

	public JsonWriterPipline(String path) {
		setPath(path);
	}

	public JsonWriterPipline(String path, Integer limitCnt) {
		setPath(path);
		this.limitCnt = limitCnt;
	}

	public JsonWriterPipline(String path, Integer limitCnt, String spiderType) {
		setPath(path);
		this.limitCnt = limitCnt;
		this.spiderType = spiderType;
	}

	public void setSpiderType(String spiderType) {
		this.spiderType = spiderType;
	}

	public void setLimitCnt(Integer limitCnt) {
		this.limitCnt = limitCnt;
	}

	private void setPath(String path) {
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	@Override
	public void process(ResultItems items, Task task) {
		Object result = items.get("result");
		if (result == null) {
			logger.info("cannot find result.");
		}else{
			try {
				write(JSON.toJSONString(result));
			} catch (IOException e) {
				logger.info(this.getClass().getName() + " process error.", e);
			}
		}
	}

	private void write(String string) throws IOException {
		checkOutStream();
		bufferedWriter.append(string);
		bufferedWriter.newLine();
		bufferedWriter.flush();
	}

	private synchronized void checkOutStream() throws IOException {
		if (cnt++ >= limitCnt) {
			closeOutputStream();
			file.renameTo(new File(file.getAbsolutePath().replace(".tmp", ".out")));
			cnt = 1;
		}

		if (fileOutputStream == null || outputStreamWriter == null || bufferedWriter == null) {
			createOutputStream();
		}
	}

	/**
	 * 重新写文件：tmp文件改为out文件
	 * 
	 * @return
	 */
	public synchronized boolean flush() {
		try {
			closeOutputStream();
			if (file != null && file.exists()) {
				file.renameTo(new File(file.getAbsolutePath().replace(".tmp", ".out")));
			}
			
			cnt = 1;
			return true;
		} catch (Exception e) {
			logger.error("flush error.", e);
			return false;
		}
	}

	private void createOutputStream() throws IOException {
		file = createFile();
		fileOutputStream = new FileOutputStream(file);
		outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		bufferedWriter = new BufferedWriter(outputStreamWriter);
	}

	/**
	 * 关闭流对象
	 */
	private void closeOutputStream() {
		try {
			if (bufferedWriter != null) {
				bufferedWriter.flush();
				bufferedWriter.close();
			}
		} catch (Exception e) {
			logger.info("bufferedOutputStream close error.", e);
		} finally {
			bufferedWriter = null;
		}

		try {
			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}
		} catch (Exception e) {
			logger.info("closeOutputStream close error.", e);
		} finally {
			outputStreamWriter = null;
		}

		try {
			if (fileOutputStream != null)
				fileOutputStream.close();
		} catch (Exception e) {
			logger.info("fileOutputStream close error.", e);
		} finally {
			fileOutputStream = null;
		}
	}

	/**
	 * 根据日期，类型创建文件
	 * 
	 * @return
	 */
	private File createFile() {
		// 拼接完整的目录路径，加上日期部分
		String _dir = getPath() + createDirDatePart() + File.separator;

		// 判断目录是否存在，如不存在则新建目录
		File dirFile = new File(_dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		// 拼接完整的文件名
		String fileName = String.format("%s_%s_%s_%s_%s.tmp", createFileNameDatePart(), getRandomPart(), spiderType,
				getPcName(), getProcessId());

		File file = new File(_dir, fileName);
		// 如果文件名已经存在则重新创建文件
		if (file.exists()) {
			file = createFile();
		}

		return file;
	}

	/**
	 * 获取计算机名称，文件名中的一个组成部分
	 * 
	 * @return
	 */
	private String getPcName() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			// 获取本机计算机名称
			String hostName = addr.getHostName().toString();
			return hostName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取四位随机数，文件名中的一个组成部分
	 * 
	 * @return
	 */
	private String getRandomPart() {
		Random random = new Random();
		int value = random.nextInt(10000);
		DecimalFormat fmt = new DecimalFormat("0000");
		return fmt.format(value);
	}

	/**
	 * 构建文件名中的日期部分（yyyyMMddHHmmss格式）
	 * 
	 * @return
	 */
	private String createFileNameDatePart() {
		Date date = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
		return fmt.format(date);
	}

	/**
	 * 获取当前进程id，文件名中的一个组成部分
	 * 
	 * @return
	 */
	private String getProcessId() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		// get pid
		String pid = name.split("@")[0];
		long threadId = Thread.currentThread().getId();
		return pid + "_" + threadId;
	}

	/**
	 * 构造目录的日期部分（yyyy-MM-dd格式）
	 * 
	 * @return
	 */
	private String createDirDatePart() {
		Date date = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		return fmt.format(date);
	}
}
