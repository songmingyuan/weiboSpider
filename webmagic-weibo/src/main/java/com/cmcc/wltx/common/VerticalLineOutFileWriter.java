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

import org.apache.commons.lang.StringUtils;

import com.cmcc.wltx.model.Article;
import com.cmcc.wltx.utils.HtmlUtils;

/**
 * 竖线分隔格式的out文件书写器，每条数据占一行，各字段以“|”分隔，以“#”结束，字段顺序如下
 * 
 * <pre>
 * RECNO|ID|RECTYPE|THESOURCE|REFERENCE|DATE|FFDCREATE|LANGUAGETYPE|DRESOURCE|TITLE|CONTENT|ABSTRACT|RECEMOTIONAL|AREA|FREQUENCYWORD|LIKEINFO|LIKEINFOCOUNT|SCREEN_NAME|COMMENTS|REPORTCOUNT|READCOUNT|WEIBOTYPE|WEIXINTYPE|HOTVALUE|MEDIATYPE|ALARMLEVEL|KEYWORD|BUSINESSTYPE|#
 * </pre>
 * 
 * @author liping
 * 
 */
public class VerticalLineOutFileWriter {
	public static final String RECTYPE_APP = "appspider";
	public static final String RECTYPE_WEB = "webspider";
	public static final String RECTYPE_BBS = "bbsspider";
	public static final String RECTYPE_BLOG = "blogspider";
	public static final String RECTYPE_WEIXIN = "weixinspider";
	private static final String SEPARATOR = "|";
	private static final String END = "#\r\n";

	private static ThreadLocal<VerticalLineOutFileWriter> threaadLocal = new ThreadLocal<VerticalLineOutFileWriter>();
	
	private static final Vector<VerticalLineOutFileWriter> writerList = new Vector<VerticalLineOutFileWriter>();

	/**
	 * 获取OutFileWriter实例，不指定目录与爬虫类型 写文件的时候直接指定文件路径
	 * 
	 * @return
	 */
	public static VerticalLineOutFileWriter getInstance() {
		if (threaadLocal.get() == null) {
			VerticalLineOutFileWriter writer = new VerticalLineOutFileWriter();
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 获取VerticalLineOutFileWriter实例，每个文件最多100条记录
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型 网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫 appspider
	 *            微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @return
	 */
	public static VerticalLineOutFileWriter getInstance(String dir, String recType) {
		if (threaadLocal.get() == null) {
			VerticalLineOutFileWriter writer = new VerticalLineOutFileWriter(dir, recType);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 获取VerticalLineOutFileWriter实例
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型：网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫 appspider
	 *            微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @param size
	 *            每个out文件中最大记录数
	 * @return
	 */
	public static VerticalLineOutFileWriter getInstance(String dir, String recType, int size) {
		if (threaadLocal.get() == null) {
			VerticalLineOutFileWriter writer = new VerticalLineOutFileWriter(dir, recType, size);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 获取VerticalLineOutFileWriter实例
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型 网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫 appspider
	 *            微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @param extension
	 *            正式文件扩展名
	 * @param size
	 *            每个out文件中最大记录数
	 * @return
	 */
	public static VerticalLineOutFileWriter getInstance(String dir, String recType, String extension, int size) {
		if (threaadLocal.get() == null) {
			VerticalLineOutFileWriter writer = new VerticalLineOutFileWriter(dir, recType, extension, size);
			threaadLocal.set(writer);
			writerList.add(writer);
		}
		return threaadLocal.get();
	}

	/**
	 * 获取VerticalLineOutFileWriter实例，每个out文件中最大记录数为100
	 * 
	 * @param dir
	 *            out文件存放目录，不包括日期部分
	 * @param recType
	 *            爬虫类型 网页模板爬虫 webspider 自动化模板爬虫 webautospider APP爬虫 appspider
	 *            微博爬虫 weibospider 微信爬虫 weixinspider 流媒体爬虫 tvstream
	 * @param extension
	 *            正式文件扩展名
	 * @return
	 */
	public static VerticalLineOutFileWriter getInstance(String dir, String recType, String extension) {
		if (threaadLocal.get() == null) {
			VerticalLineOutFileWriter writer = new VerticalLineOutFileWriter(dir, recType, extension);
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

	private VerticalLineOutFileWriter() {
		super();
	}

	private VerticalLineOutFileWriter(String dir, String recType) {
		this(dir, recType, 100);
	}

	private VerticalLineOutFileWriter(String dir, String recType, int size) {
		this.dir = dir;
		this.recType = recType;
		this.size = size;
	}

	private VerticalLineOutFileWriter(String dir, String recType, String extension) {
		this(dir, recType, extension, 100);
	}

	private VerticalLineOutFileWriter(String dir, String recType, String extension, int size) {
		this(dir, recType, size);
		this.extension = extension;
	}

	/**
	 * 过滤null值
	 * 
	 * @param src
	 * @return
	 */
	private String filterNull(Object src) {
		if (src == null)
			return "";
		return src.toString();
	}

	/**
	 * 对字符串中的换行符、竖线进行转义
	 * 
	 * @param src
	 * @return
	 */
	private String escape(String src) {
		if (StringUtils.isEmpty(src))
			return filterNull(src);
		return src.replace("\r", "&#114;").replace("\n", "&#110;").replace("|", "&#124;");
	}

	/**
	 * 对字符串中的换行符、竖线进行反转义
	 * 
	 * @param escape
	 * @return
	 */
	private String unescape(String escape) {
		if (StringUtils.isEmpty(escape))
			return filterNull(escape);
		return escape.replace("&#114;", "\r").replace("&#110;", "\n").replace("&#124;", "|");
	}

	/**
	 * 将article对象转为out文件固定的格式输出
	 * 
	 * @return
	 */
	private String articleToString(Article article, int index) {
		StringBuilder builder = new StringBuilder();
		builder.append(index).append(SEPARATOR);
		builder.append(
				article.getId() == null ? MD5.getMD5(article.getReference()) == null ? "" : MD5.getMD5(article.getReference()) : article.getId())
				.append(SEPARATOR);
		builder.append(filterNull(article.getRecType())).append(SEPARATOR);
		builder.append(article.getTheSource() == null ? "" : article.getTheSource()).append(SEPARATOR);
		builder.append(filterNull(article.getReference())).append(SEPARATOR);
		builder.append(article.getDate() == null ? "" : article.getDate().getTime() / 1000).append(SEPARATOR);
		builder.append(article.getFfdCreate() == null ? "" : article.getFfdCreate().getTime() / 1000).append(SEPARATOR);
		builder.append("utf-8").append(SEPARATOR);// 默认utf-8
		builder.append(escape(article.getDreSource())).append(SEPARATOR);
		builder.append(escape(HtmlUtils.removeHtmlTags(article.getTitle()))).append(SEPARATOR);
		builder.append(escape(HtmlUtils.removeHtmlTags(article.getContent()))).append(SEPARATOR);
		builder.append(escape(article.getSummary())).append(SEPARATOR);
		builder.append(filterNull(article.getRecEmotional())).append(SEPARATOR);
		builder.append(escape(article.getArea())).append(SEPARATOR);
		builder.append(filterNull(article.getFrequencyWord())).append(SEPARATOR);
		builder.append(filterNull(article.getLikeInfo())).append(SEPARATOR);
		builder.append(filterNull(article.getLikeInfoCount())).append(SEPARATOR);
		builder.append(escape(article.getScreenName())).append(SEPARATOR);
		builder.append(filterNull(article.getComments())).append(SEPARATOR);
		builder.append(filterNull(article.getReportCount())).append(SEPARATOR);
		builder.append(filterNull(article.getReadCount())).append(SEPARATOR);
		builder.append(filterNull(article.getWeiboType())).append(SEPARATOR);
		builder.append(filterNull(article.getWeixinType())).append(SEPARATOR);
		builder.append(filterNull(article.getHotValue())).append(SEPARATOR);
		builder.append(filterNull(article.getMediaType())).append(SEPARATOR);
		builder.append(filterNull(article.getAlarmLevel())).append(SEPARATOR);
		builder.append(filterNull(article.getKeyWord())).append(SEPARATOR);
		builder.append(filterNull(article.getBusinessType())).append(SEPARATOR);
		builder.append(END);
		return builder.toString();
	}

	/**
	 * 将article对象写入到out文件
	 * 
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
	 * 
	 * @param path
	 * @param articles
	 */
	public void writeArticle(String path, List<Article> articles) throws IOException {
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

		// 如果文件名已经存在则重新创建文件
		if (file.exists()) {
			file = createFile();
		}
		return file;
	}

	/**
	 * 关闭输出流
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (out != null){
			out.close();
			out = null;
		}
		if (currentFilePath != null) {
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
		for (VerticalLineOutFileWriter writer : writerList) {
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
	 * 获取一个新的VerticalLineOutFileWriter对象
	 * @param dir
	 * @param recType
	 * @param size
	 * @return
	 */
	public static VerticalLineOutFileWriter getNewWriter(String dir, String recType, int size) {
		return new VerticalLineOutFileWriter(dir, recType, size);
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

	public static void main(String[] args) throws IOException {
		VerticalLineOutFileWriter writer = VerticalLineOutFileWriter.getInstance();
		String[] src = { "aaa\r\n", "bbb\n", "ccc", "|ddd", "e|e", "ff\rf|f\n", "中文\r" };
		StringBuilder builder = new StringBuilder();
		for (String string : src) {
			builder.append(writer.escape(string)).append("|");
		}
		String s1 = builder.toString();
		String[] dst = s1.split("\\|");
		for (String string : dst) {
		}
	}
}
