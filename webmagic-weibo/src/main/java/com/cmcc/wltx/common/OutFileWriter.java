package com.cmcc.wltx.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.cmcc.wltx.model.Article;

public class OutFileWriter {
	private static final String LINE = "\r\n";

	private static ThreadLocal<OutFileWriter> threaadLocal = new ThreadLocal<OutFileWriter>();
	private static final Vector<OutFileWriter> writerList = new Vector<OutFileWriter>();
	
	/**
	 * 获取OutFileWriter实例，不指定目录与爬虫类型
	 * 写文件的时候直接指定文件路径
	 * @return
	 */
	public static OutFileWriter getInstance(){
		if (threaadLocal.get() == null) {
			OutFileWriter writer = new OutFileWriter();
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 获取OutFileWriter实例，每个文件最多100条记录
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型 网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫 appspider
	 *            微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @return
	 */
	public static OutFileWriter getInstance(String dir, String recType) {
		if (threaadLocal.get() == null) {
			OutFileWriter writer = new OutFileWriter(dir, recType);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 获取OutFileWriter实例
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型
	 * @param size
	 *            每个out文件中最大记录数 网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫
	 *            appspider 微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @return
	 */
	public static OutFileWriter getInstance(String dir, String recType, int size) {
		if (threaadLocal.get() == null) {
			OutFileWriter writer = new OutFileWriter(dir, recType, size);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}
	
	/**
	 * 获取OutFileWriter实例
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型:网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫
	 *            appspider 微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @param extension
	 * 			  正式文件扩展名
	 * @param size
	 *            每个out文件中最大记录数 
	 * @return
	 */
	public static OutFileWriter getInstance(String dir, String recType, String extension,int size) {
		if (threaadLocal.get() == null) {
			OutFileWriter writer = new OutFileWriter(dir, recType, extension, size);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}
	
	/**
	 * 获取OutFileWriter实例，每个文件最多100条记录
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型:网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫
	 *            appspider 微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @param extension
	 * 			  正式文件扩展名
	 * @return
	 */
	public static OutFileWriter getInstance(String dir, String recType, String extension) {
		if (threaadLocal.get() == null) {
			OutFileWriter writer = new OutFileWriter(dir, recType, extension);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 每个文件中所包含的最大文件数
	 */
	private int size;

	/**
	 * 存放目录，不包括日期部分
	 */
	private String dir;

	/**
	 * 计数器
	 */
	private int index = 1;

	/**
	 * 爬虫类型 网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫 appspider 微博爬虫
	 * weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 */
	private String recType;

	/**
	 * 正式文件的扩展名,默认为.out
	 */
	private String extension = ".out";
	
	private FileOutputStream out;
	
	/**
	 * 当前文件的绝对路径
	 */
	private String currentFilePath;
	
	
	private OutFileWriter(){
		super();
	}

	private OutFileWriter(String dir, String recType) {
		this(dir, recType, 100);
	}

	private OutFileWriter(String dir, String recType, int size) {
		this.dir = dir;
		this.recType = recType;
		this.size = size;
	}
	
	private OutFileWriter(String dir, String recType, String extension){
		this(dir, recType, extension, 100);
	}
	
	private OutFileWriter(String dir, String recType, String extension, int size){
		this(dir, recType, size);
		this.extension = extension;
	}

	/**
	 * 将article对象转为out文件固定的格式输出
	 * 
	 * @return
	 */
	private String articleToString(Article article, int index) {
		StringBuilder builder = new StringBuilder();
		builder.append("#BEGINDOC").append(LINE);
		builder.append("#RECNO=").append(index).append(LINE);
		builder.append("#ID=").append(article.getId() == null ? MD5.getMD5(article.getReference()) == null ? "" : MD5.getMD5(article.getReference()) : article.getId()).append(LINE);
		builder.append("#RECTYPE=").append(article.getRecType() == null ? "" : article.getRecType()).append(LINE);
		builder.append("#THESOURCE=").append(article.getTheSource() == null ? "" : article.getTheSource()).append(LINE);
		builder.append("#REFERENCE=").append(article.getReference() == null ? "" : article.getReference()).append(LINE);
		builder.append("#DATE=").append(article.getDate() == null ? "" : article.getDate().getTime() / 1000).append(LINE);
		builder.append("#FFDCREATE=").append(article.getFfdCreate() == null ? "" : article.getFfdCreate().getTime() / 1000).append(LINE);
		builder.append("#LANGUAGETYPE=utf-8").append(LINE);// 默认utf-8
		builder.append("#DRESOURCE=").append(article.getDreSource() == null ? "" : article.getDreSource()).append(LINE);
		builder.append("#TITLE=").append(article.getTitle() == null ? "" : article.getTitle()).append(LINE);
		builder.append("#CONTENT=").append(article.getContent() == null ? "" : article.getContent()).append(LINE);
		builder.append("#ABSTRACT=").append(article.getSummary() == null ? "" : article.getSummary()).append(LINE);
		builder.append("#RECEMOTIONAL=").append(article.getRecEmotional() == null ? "" : article.getRecEmotional()).append(LINE);
		builder.append("#AREA=").append(article.getArea() == null ? "" : article.getArea()).append(LINE);
		builder.append("#FREQUENCYWORD=").append(article.getFrequencyWord() == null ? "" : article.getFrequencyWord()).append(LINE);
		builder.append("#LIKEINFO=").append(article.getLikeInfo() == null ? "" : article.getLikeInfo()).append(LINE);
		builder.append("#LIKEINFOCOUNT=").append(article.getLikeInfoCount() == null ? "" : article.getLikeInfoCount()).append(LINE);
		builder.append("#SCREEN_NAME=").append(article.getScreenName() == null ? "" : article.getScreenName()).append(LINE);
		builder.append("#COMMENTS=").append(article.getComments() == null ? "" : article.getComments()).append(LINE);
		builder.append("#REPORTCOUNT=").append(article.getReportCount() == null ? "" : article.getReportCount()).append(LINE);
		builder.append("#READCOUNT=").append(article.getReadCount() == null ? "" : article.getReadCount()).append(LINE);
		builder.append("#WEIBOTYPE=").append(article.getWeiboType() == null ? "" : article.getWeiboType()).append(LINE);
		builder.append("#WEIXINTYPE=").append(article.getWeixinType() == null ? "" : article.getWeixinType()).append(LINE);
		builder.append("#HOTVALUE=").append(article.getHotValue() == null ? "" : article.getHotValue()).append(LINE);
		builder.append("#MEDIATYPE=").append(article.getMediaType() == null ? "" : article.getMediaType()).append(LINE);
		builder.append("#ALARMLEVEL=").append(article.getAlarmLevel() == null ? "" : article.getAlarmLevel()).append(LINE);
		builder.append("#KEYWORD=").append(article.getKeyWord() == null ? "" : article.getKeyWord()).append(LINE);
		builder.append("#ENDDOC").append(LINE);
		return builder.toString();
	}

	/**
	 * 将article对象写入到out文件
	 * @param article
	 * @throws IOException 
	 */
	public void writeArticle(Article article) throws IOException {
		if (out == null || index > size) {
			close();
			out = new FileOutputStream(createFile());
		}
		out.write(articleToString(article, index++).getBytes());
		out.flush();
	}
	
	/**
	 * 将一组article对象写入指定的文件
	 * @param path
	 * @param articles
	 */
	public void writeArticle(String path,List<Article> articles) throws IOException{
		FileOutputStream out = new FileOutputStream(path);
		int index = 1;
		for (Article article : articles) {
			out.write(articleToString(article, index++).getBytes());
			out.flush();
		}
		out.close();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
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
	 * 创建out文件File实例
	 * 
	 * @return
	 */
	private File createFile() {
		// 拼接完整的目录路径，加上日期部分
		if (!dir.endsWith("/") && !dir.endsWith("\\")) {
			dir += File.separator;
		}
		String _dir = dir + createDirDatePart();

		// 判断目录是否存在，如不存在则新建目录
		File dirFile = new File(_dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		// 拼接完整的文件名
		String fileName = String.format("%s_%s_%s_%s_%s.tmp", createFileNameDatePart(), getRandomPart(), recType, getPcName(), getProcessId());

		File file = new File(_dir, fileName);
		
		currentFilePath = file.getAbsolutePath();
		
		//如果文件名已经存在则重新创建文件
		if(file.exists()){
			file = createFile();
		}
		return file;
	}

	/**
	 * 关闭输出流
	 * @throws IOException 
	 */
	public void close() throws IOException {
		if (out != null){
			out.close();
			out = null;
		}
		if(currentFilePath != null){
			File tmpFile = new File(currentFilePath);
			if(tmpFile.exists()){
				File dest = new File(currentFilePath.replace(".tmp", extension));
				tmpFile.renameTo(dest);
			}
		}
		index = 1;
	}
	
	/**
	 * 重命名所有tmp文件
	 */
	public static void closeAll(){
		for (OutFileWriter writer : writerList) {
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 获取一个新的OutFileWriter对象
	 * @param dir
	 * @param recType
	 * @param size
	 * @return
	 */
	public static OutFileWriter getNewWriter(String dir, String recType, int size) {
		return new OutFileWriter(dir, recType, size);
	}
	
	/**
	 * 将一组article对象写入指定的文件
	 * @param path
	 * @param articles
	 */
	public void writeArticle(List<Article> articles) throws IOException{
		FileOutputStream out = new FileOutputStream(createFile());
		int index = 1;
		for (Article article : articles) {
			out.write(articleToString(article, index++).getBytes());
			out.flush();
		}
		out.close();
	}

}
