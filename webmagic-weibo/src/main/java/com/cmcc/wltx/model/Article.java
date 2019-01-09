package com.cmcc.wltx.model;

import java.util.Date;
import java.util.Properties;
import java.util.Set;


/****
 * 
 * @author liping
 *
#BEGINDOC
#RECNO=序号
#ID=唯一标识（url的md5值）
#RECTYPE=信源类型（1.新闻，2.微博，3.微信，4.APP，5.报刊，6.论坛，7.博客，8.视频，9.商机）
#THESOURCE=采集来源
#REFERENCE=原文链接地址
#DATE=采集时间，时间戳格式，如1444879817
#FFDCREATE=发布时间，时间戳格式，如1444878845
#LANGUAGETYPE=编码字符集
#DRESOURCE=原始来源
#TITLE=标题
#CONTENT=内容
#ABSTRACT=摘要
#RECEMOTIONAL=情感类型（1.正面，0.中立，-1.负面）
#AREA=地域信息
#FREQUENCYWORD=高频词
#LIKEINFO=相似舆情的链接地址，多个用“,”
#LIKEINFOCOUNT=相似舆情数量
#SCREEN_NAME=作者名称
#COMMENTS=评论数
#REPORTCOUNT=转发数
#READCOUNT=浏览量
#WEIBOTYPE=微博用户类型，如果不是微博此字段可以为空（1.蓝V，2.橙V，3.微博达人，4.草根大V）
#WEIXINTYPE=微信用户类型，如果不是微信此字段的值可以为空（1.认证用户，2.自媒体用户）
#HOTVALUE=舆情热度值
#MEDIATYPE=媒体类型（1.全国性媒体，2.地方性媒体）
#KEYWORD=对应词条配置表的ID值
#ALARMLEVEL=告警级别
#ENDDOC
 */

public class Article {
	public static final int RECTYPE_NEWS = 1;
	public static final int RECTYPE_WEIBO = 2;
	public static final int RECTYPE_WEIXIN = 3;
	public static final int RECTYPE_APP = 4;
	public static final int RECTYPE_PAPER = 5;
	public static final int RECTYPE_BBS = 6;
	public static final int RECTYPE_BLOG = 7;
	public static final int RECTYPE_SHANGJI = 9;
	public static final int RECTYPE_SHANGJIA = 10;
	
	/**
	 * ID=唯一标识（url的md5值）
	 */
	private String id;
	
	/**
	 * RECTYPE=信源类型（1.新闻，2.微博，3.微信，4.APP，5.报刊，6.论坛，7.博客，8.视频）
	 */
	private Integer recType;
	
	/**
	 * THESOURCE=采集来源
	 */
	private String theSource;
	
	/**
	 * REFERENCE=原文链接地址
	 */
	private String reference;
	
	/**
	 * DATE=采集时间，时间戳格式，如1444879817
	 */
	private Date date;
	
	/**
	 * FFDCREATE=发布时间，时间戳格式，如1444878845
	 */
	private Date ffdCreate;
	
	/**
	 * DRESOURCE=原始来源
	 */
	private String dreSource;
	
	/**
	 * TITLE=标题
	 */
	private String title;
	
	/**
	 * CONTENT=内容
	 */
	private String content;
	
	/**
	 * ABSTRACT=摘要
	 */
	private String summary;
	
	/**
	 * RECEMOTIONAL=情感类型（1.正面，0.中立，-1.负面）
	 */
	private Integer recEmotional;
	
	/**
	 * AREA=地域信息
	 */
	private String area;
	
	/**
	 * FREQUENCYWORD=高频词
	 */
	private String frequencyWord;
	
	/**
	 * LIKEINFO=相似舆情的链接地址，多个用“,”
	 */
	private String likeInfo;
	
	/**
	 * LIKEINFOCOUNT=相似舆情数量
	 */
	private Integer likeInfoCount;
	
	/**
	 * SCREEN_NAME=作者名称
	 */
	private String screenName;
	
	/**
	 * COMMENTS=评论数
	 */
	private Integer comments;
	
	/**
	 * REPORTCOUNT=转发数
	 */
	private Integer reportCount;
	
	/**
	 * READCOUNT=浏览量
	 */
	private Integer readCount;
	
	/**
	 * WEIBOTYPE=微博用户类型，如果不是微博此字段可以为空（1.蓝V，2.橙V，3.微博达人，4.草根大V）
	 */
	private Integer weiboType;
	
	/**
	 * WEIXINTYPE=微信用户类型，如果不是微信此字段的值可以为空（1.认证用户，2.自媒体用户）
	 */
	private Integer weixinType;
	
	/**
	 * HOTVALUE=舆情热度值
	 */
	private Integer hotValue;
	
	/**
	 * MEDIATYPE=媒体类型（1.全国性媒体，2.地方性媒体）
	 */
	private Integer mediaType;
	
	/**
	 * KEYWORD=对应词条配置表的ID值
	 */
	private String keyWord;
	
	/**
	 * ALARMLEVEL=告警级别
	 */
	private Integer alarmLevel;

	/**
	 * businessType=业务类型
	 */
	private String businessType;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getRecType() {
		return recType;
	}

	public void setRecType(Integer recType) {
		this.recType = recType;
	}

	public String getTheSource() {
		return theSource;
	}

	public void setTheSource(String theSource) {
		this.theSource = theSource;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getFfdCreate() {
		return ffdCreate;
	}

	public void setFfdCreate(Date ffdCreate) {
		this.ffdCreate = ffdCreate;
	}

	public String getDreSource() {
		return dreSource;
	}

	public void setDreSource(String dreSource) {
		this.dreSource = dreSource;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Integer getRecEmotional() {
		return recEmotional;
	}

	public void setRecEmotional(Integer recEmotional) {
		this.recEmotional = recEmotional;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getFrequencyWord() {
		return frequencyWord;
	}

	public void setFrequencyWord(String frequencyWord) {
		this.frequencyWord = frequencyWord;
	}

	public String getLikeInfo() {
		return likeInfo;
	}

	public void setLikeInfo(String likeInfo) {
		this.likeInfo = likeInfo;
	}

	public Integer getLikeInfoCount() {
		return likeInfoCount;
	}

	public void setLikeInfoCount(Integer likeInfoCount) {
		this.likeInfoCount = likeInfoCount;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public Integer getComments() {
		return comments;
	}

	public void setComments(Integer comments) {
		this.comments = comments;
	}

	public Integer getReportCount() {
		return reportCount;
	}

	public void setReportCount(Integer reportCount) {
		this.reportCount = reportCount;
	}

	public Integer getReadCount() {
		return readCount;
	}

	public void setReadCount(Integer readCount) {
		this.readCount = readCount;
	}

	public Integer getWeiboType() {
		return weiboType;
	}

	public void setWeiboType(Integer weiboType) {
		this.weiboType = weiboType;
	}

	public Integer getWeixinType() {
		return weixinType;
	}

	public void setWeixinType(Integer weixinType) {
		this.weixinType = weixinType;
	}

	public Integer getHotValue() {
		return hotValue;
	}

	public void setHotValue(Integer hotValue) {
		this.hotValue = hotValue;
	}

	public Integer getMediaType() {
		return mediaType;
	}

	public void setMediaType(Integer mediaType) {
		this.mediaType = mediaType;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public Integer getAlarmLevel() {
		return alarmLevel;
	}

	public void setAlarmLevel(Integer alarmLevel) {
		this.alarmLevel = alarmLevel;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public static void main(String[] args) {
		Properties props = System.getProperties();
		Set<Object> keys = props.keySet();
		for (Object key : keys) {
		}
	}
	
}
