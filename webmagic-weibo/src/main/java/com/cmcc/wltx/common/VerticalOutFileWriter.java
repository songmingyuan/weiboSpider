package com.cmcc.wltx.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.cmcc.wltx.model.Article;
import com.cmcc.wltx.utils.HtmlUtils;
import com.cmcc.wltx.utils.KafkaPropertiesLoader;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

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
public class VerticalOutFileWriter {
	public static final String RECTYPE_WEIBO = "weibospider";
	public static final String RECTYPE_APP = "appspider";
	public static final String RECTYPE_WEB = "webspider";
	public static final String RECTYPE_BBS = "bbsspider";
	public static final String RECTYPE_BLOG = "blogspider";
	public static final String RECTYPE_WEIXIN = "weixinspider";
	public static final String RECTYPE_PAPER = "paperspider";
	public static final String RECTYPE_SHANGJI = "shangjispider";
	public static final String RECTYPE_SHANGJIA = "shangjiaspider";
	private static final String SEPARATOR = "|";
	private static final String END = "#\r\n";

	private static final Map<Integer, VerticalOutFileWriter> writerMap = new HashMap<Integer, VerticalOutFileWriter>();

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
	public static VerticalOutFileWriter getInstance(String dir, Integer sourceType) {
		return getInstance(dir, sourceType, ".out", 100);
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
	public static VerticalOutFileWriter getInstance(String dir, Integer sourceType, int size) {
		return getInstance(dir, sourceType, ".out", size);
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
	public static VerticalOutFileWriter getInstance(String dir, Integer sourceType, String extension, int size) {
		synchronized (writerMap) {
			VerticalOutFileWriter writer = writerMap.get(sourceType);
			if (null == writer) {
				String recType;
				switch (sourceType) {
				case com.cmcc.wltx.model.Article.RECTYPE_NEWS:
					recType = VerticalOutFileWriter.RECTYPE_WEB;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_BBS:
					recType = VerticalOutFileWriter.RECTYPE_BBS;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_BLOG:
					recType = VerticalOutFileWriter.RECTYPE_BLOG;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_WEIXIN:
					recType = VerticalOutFileWriter.RECTYPE_WEIXIN;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_APP:
					recType = VerticalOutFileWriter.RECTYPE_APP;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_WEIBO:
					recType = VerticalOutFileWriter.RECTYPE_WEIBO;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_PAPER:
					recType = VerticalOutFileWriter.RECTYPE_PAPER;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_SHANGJI:
					recType = VerticalOutFileWriter.RECTYPE_SHANGJI;
					break;
				case com.cmcc.wltx.model.Article.RECTYPE_SHANGJIA:
					recType = VerticalOutFileWriter.RECTYPE_SHANGJIA;
					break;
				default:
					throw new Error("无效的信源类型 - " + sourceType);
				}
				writer = new VerticalOutFileWriter(dir, recType, extension, size);
				writerMap.put(sourceType, writer);
			}
			return writer;
		}
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
	public static VerticalOutFileWriter getInstance(String dir, Integer sourceType, String extension) {
		return getInstance(dir, sourceType, extension, 100);
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
	private String extension;

	private FileOutputStream out;
	
	private Producer<String, String> producer;
	
	private static final KafkaPropertiesLoader pptLoader = new KafkaPropertiesLoader();

	/**
	 * 当前文件的绝对路径
	 */
	private String currentFilePath;

	private VerticalOutFileWriter(String dir, String recType, String extension, int size) {
		if (!dir.endsWith("/") && !dir.endsWith("\\")) {
			dir += File.separator;
		}
		this.dir = dir;
		this.recType = recType;
		this.size = size;
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
	 * 将article对象转为out文件固定的格式输出
	 * 
	 * @return
	 */
	private String articleToString(Article article, int index) {
		StringBuilder builder = new StringBuilder();
		builder.append(index).append(SEPARATOR);
		String id = article.getId();
		if (null == id) {
			id = UUID.randomUUID().toString().replace("-", "");
		}
		builder.append(id).append(SEPARATOR);
		// builder.append(
		// article.getId() == null ? MD5.getMD5(article.getReference()) == null
		// ? "" : MD5.getMD5(article.getReference()) : article.getId())
		// .append(SEPARATOR);
		builder.append(filterNull(article.getRecType())).append(SEPARATOR);
		builder.append(escape(article.getTheSource() == null ? "" : article.getTheSource())).append(SEPARATOR);
		builder.append(filterNull(article.getReference())).append(SEPARATOR);
		builder.append(article.getDate() == null ? "" : article.getDate().getTime() / 1000).append(SEPARATOR);
		builder.append(article.getFfdCreate() == null ? "" : article.getFfdCreate().getTime() / 1000).append(SEPARATOR);
		builder.append("utf-8").append(SEPARATOR);// 默认utf-8
		builder.append(escape(article.getDreSource())).append(SEPARATOR);
		builder.append(escape(HtmlUtils.removeHtmlTags(article.getTitle()))).append(SEPARATOR);
		Integer recType = article.getRecType();
		// 商机信源不过滤HTML标签
		if (recType != null && Article.RECTYPE_SHANGJI == recType) {
			builder.append(escape(article.getContent())).append(SEPARATOR);
		} else {
			builder.append(escape(HtmlUtils.removeHtmlTags(article.getContent()))).append(SEPARATOR);
		}
		builder.append(escape(article.getSummary())).append(SEPARATOR);
		builder.append(filterNull(article.getRecEmotional())).append(SEPARATOR);
		builder.append(escape(article.getArea())).append(SEPARATOR);
		builder.append(filterNull(article.getFrequencyWord())).append(SEPARATOR);
		builder.append(escape(filterNull(article.getLikeInfo()))).append(SEPARATOR);
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
	public synchronized void writeArticle(Article article) throws IOException {
		if (out == null || index > size) {
			close();
			File file = createFile();
			out = new FileOutputStream(file);
			currentFilePath = file.getAbsolutePath();
		}
		out.write(articleToString(article, index++).getBytes());
	}
	
	/**
	 * 将article对象写入到kafka
	 * 
	 * @param article
	 * @throws IOException
	 */
	public synchronized void writeArticleToKafka(Article article) throws IOException {
		// 加载producer配置
		if (producer == null || index > size) {
			closeProducer();
			producer = new Producer<String, String>(pptLoader.getConfig());
		}
		//TODO 日志输出，格式待定
		KeyedMessage<String, String> message = new KeyedMessage<String, String>(pptLoader.getTopic(),
				articleToString(article, index++));
		producer.send(message);
	}

	/**
	 * 将一组article对象写入指定的文件
	 * 
	 * @param path
	 * @param articles
	 */
	public void writeArticle(String path, List<Article> articles) throws IOException {
		FileOutputStream out = new FileOutputStream(path);
		int i = 1;
		for (Article article : articles) {
			out.write(articleToString(article, i++).getBytes());
			out.flush();
		}
		out.close();
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
		String _dir = dir + createDirDatePart();

		// 判断目录是否存在，如不存在则新建目录
		File dirFile = new File(_dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		// 拼接完整的文件名
		String fileName = String.format("%s_%s_%s_%s_%s.tmp", createFileNameDatePart(), getRandomPart(), recType,
				getPcName(), getProcessId());

		File file = new File(_dir, fileName);

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
	public synchronized void close() throws IOException {
		if (out == null) {
			return;
		}
		out.close();
		out = null;
		if (currentFilePath != null) {
			File tmpFile = new File(currentFilePath);
			if (tmpFile.exists()) {
				File dest = new File(currentFilePath.replace(".tmp", extension));
				tmpFile.renameTo(dest);
			}
		}
		index = 1;
	}
	
	/**
	 * 关闭kafka连接
	 * 
	 * @throws IOException
	 */
	public synchronized void closeProducer() throws IOException {
		if (producer == null) {
			return;
		}
		producer.close();
		producer = null;
		index = 1;
	}

	/**
	 * 重命名所有tmp文件
	 * 
	 * @throws IOException
	 */
	public static void closeAll() throws IOException {
		synchronized (writerMap) {
			for (VerticalOutFileWriter writer : writerMap.values()) {
				if (writer != null) {
					writer.close();
					writer.closeProducer();
				}
			}
		}
	}
}
