package com.cmcc.wltx.collector.spider.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.cmcc.wltx.collector.ConstantsHome;

public class Article {
	/**
	 * 记录类型（输出用）
	 */
	private Integer sourceType;
	/**
	 * 新闻链接
	 */
	private String url;
	/**
	 * 编码字符集
	 */
	private String charset = ConstantsHome.CHARSET_DEFAULT;
	/**
	 * 原始URL
	 */
	private String sourceUrl;
	/**
	 * 站点
	 */
	private String siteName;
	/**
	 * 频道
	 */
	private String channel;
	/**
	 * 分类
	 */
	private String category;
	/**
	 * 发布源
	 */
	private String pubSource;
	/**
	 * 列表页显示的标题
	 */
	private String listTitle;
	/**
	 * 长标题
	 */
	private String longTitle;
	/**
	 * 文章页标题
	 */
	private String title;
	/**
	 * 专题标题
	 */
	private String topicTitle;
	/**
	 * 专题摘要
	 */
	private String topicDesc;
	/**
	 * 专题创建日期
	 */
	private Date topicCreateDate;
	/**
	 * 专题编辑日期
	 */
	private Date topicEditDate;
	/**
	 * 专题作者
	 */
	private String topicAuthor;

	/**
	 * 作者
	 */
	private String author;
	
	/**
	 * 作者_改
	 */
	private List<String> authors;
	/**
	 * 创建日期
	 */
	private Date createDate;
	/**
	 * 编辑日期
	 */
	private Date editDate;
	/**
	 * 源日期
	 */
	private Date sourceDate;
	/**
	 * 封面
	 */
	private String cover;
	/**
	 * 摘要
	 */
	private String summary;
	/**
	 * 正文内容
	 */
	private String content;
	/**
	 * 正文图片
	 */
	private List<String> imgSrcs;
	/**
	 * 标签/关键词
	 */
	private Set<String> tags;
	/**
	 * 评论数
	 */
	private Integer commentsCount;
	/**
	 * 参与数
	 */
	private Integer participationsCount;
	/**
	 * 点赞数
	 */
	private Integer likeCount;
	/**
	 * 踩数
	 */
	private Integer dislikeCount;
	/**
	 * 转发数
	 */
	private Integer forwardCount;
	/**
	 * 点击量
	 */
	private Integer clickCount;
	/**
	 * 相关阅读
	 */
	private Set<String> relatedUrls;
	/**
	 * 类型（处理用）
	 */
	private Integer type;
	
	/**
 	 * 地域信息
 	 */
 	private String area;
 	
 	/**
 	 * 对应com.cmcc.wltx.model.Article 类中的likeInfo字段
 	 */
 	private String likeInfo;
	
	public Article(Integer sourceType) {
		super();
		this.sourceType = sourceType;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getListTitle() {
		return listTitle;
	}

	public void setListTitle(String listTitle) {
		this.listTitle = listTitle;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getPubSource() {
		return pubSource;
	}

	public void setPubSource(String pubSource) {
		this.pubSource = pubSource;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public Integer getCommentsCount() {
		return commentsCount;
	}

	public void setCommentsCount(Integer commentsCount) {
		this.commentsCount = commentsCount;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getImgSrcs() {
		return imgSrcs;
	}

	public void setImgSrcs(List<String> imgSrcs) {
		this.imgSrcs = imgSrcs;
	}

	public String getAuthor() {
		return author;
	}

	@Override
	public String toString() {
		return "Article [\n url=" + url
				+ ",\n charset=" + charset
				+ ",\n sourceUrl=" + sourceUrl
				+ ",\n siteName=" + siteName
				+ ",\n channel=" + channel
				+ ",\n category=" + category
				+ ",\n pubSource=" + pubSource
				+ ",\n listTitle=" + listTitle
				+ ",\n longTitle=" + longTitle
				+ ",\n title=" + title
				+ ",\n topicTitle=" + topicTitle
				+ ",\n topicDesc=" + topicDesc
				+ ",\n topicCreateDate=" + topicCreateDate
				+ ",\n topicEditDate=" + topicEditDate
				+ ",\n topicAuthor=" + topicAuthor
				+ ",\n author=" + author
				+ ",\n authors=" + authors
				+ ",\n createDate=" + createDate
				+ ",\n editDate=" + editDate
				+ ",\n sourceDate=" + sourceDate
				+ ",\n cover=" + cover
				+ ",\n summary=" + summary
				+ ",\n content=" + content
				+ ",\n imgSrcs=" + imgSrcs
				+ ",\n tags=" + tags
				+ ",\n commentsCount=" + commentsCount
				+ ",\n participationsCount=" + participationsCount
				+ ",\n likeCount=" + likeCount
				+ ",\n dislikeCount=" + dislikeCount
				+ ",\n forwardCount=" + forwardCount
				+ ",\n clickCount=" + clickCount
				+ ",\n relatedUrls=" + relatedUrls
				+ "\n]";
	}
	
	public String toData() {
		String currentDate = String.valueOf(System.currentTimeMillis());
		return "#BEGINDOC"
				+ "\n#RECNO=" + currentDate
				+ "\n#RECTYPE=" + 4
				+ "\n#THESOURCE=" + (siteName==null?"":siteName)
				+ "\n#REFERENCE=" + (url==null?"":url)
				+ "\n#DATE=" + currentDate
				+ "\n#FFDCREATE=" + (createDate==null?currentDate:String.valueOf(createDate.getTime()))
				+ "\n#LANGUAGETYPE=" + (charset==null?"":charset)
				+ "\n#DRESOURCE=" + (pubSource==null?"":pubSource)
				+ "\n#TITLE=" + (title==null?(listTitle==null?"":listTitle):title)
				+ "\n#CONTENT=" + (content==null?"":content)
				+ "\n#ABSTRACT=" + (summary==null?"":summary)
				+ "\n#RECEMOTIONAL=" + 0/*情感类型（1.正面，0.中立，-1.负面）*/
				+ "\n#AREA="/*地域信息*/
				+ "\n#FREQUENCYWORD="
				+ "\n#LIKEINFO="
				+ "\n#LIKEINFOCOUNT=" + 0
				+ "\n#SCREEN_NAME=" + (author==null?"":author)
				+ "\n#COMMENTS=" + (commentsCount==null?0:commentsCount)
				+ "\n#REPORTCOUNT="
				+ "\n#READCOUNT="
				+ "\n#WEIBOTYPE="
				+ "\n#WEIXINTYPE="
				+ "\n#HOTVALUE="
				+ "\n#MEDIATYPE="
				+ "\n#KEYWORD="
				+ "\n#ALARMLEVEL="
				+ "\n#ENDDOC"
				+ "\n";
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLongTitle() {
		return longTitle;
	}

	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTopicTitle() {
		return topicTitle;
	}

	public void setTopicTitle(String topicTitle) {
		this.topicTitle = topicTitle;
	}

	public String getTopicDesc() {
		return topicDesc;
	}

	public void setTopicDesc(String topicDesc) {
		this.topicDesc = topicDesc;
	}

	public Integer getParticipationsCount() {
		return participationsCount;
	}

	public void setParticipationsCount(Integer participationsCount) {
		this.participationsCount = participationsCount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	public Integer getClickCount() {
		return clickCount;
	}

	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
	}

	public Date getSourceDate() {
		return sourceDate;
	}

	public void setSourceDate(Date sourceDate) {
		this.sourceDate = sourceDate;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public Date getTopicCreateDate() {
		return topicCreateDate;
	}

	public void setTopicCreateDate(Date topicCreateDate) {
		this.topicCreateDate = topicCreateDate;
	}
	
	public String getTopicAuthor() {
		return topicAuthor;
	}

	public void setTopicAuthor(String topicAuthor) {
		this.topicAuthor = topicAuthor;
	}

	public Date getTopicEditDate() {
		return topicEditDate;
	}

	public void setTopicEditDate(Date topicEditDate) {
		this.topicEditDate = topicEditDate;
	}

	public Date getEditDate() {
		return editDate;
	}

	public void setEditDate(Date editDate) {
		this.editDate = editDate;
	}

	public Integer getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(Integer dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Set<String> getRelatedUrls() {
		return relatedUrls;
	}

	public void setRelatedUrls(Set<String> relatedUrls) {
		this.relatedUrls = relatedUrls;
	}

	public Integer getForwardCount() {
		return forwardCount;
	}

	public void setForwardCount(Integer forwardCount) {
		this.forwardCount = forwardCount;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public Integer getSourceType() {
		return sourceType;
	}

	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getLikeInfo() {
		return likeInfo;
	}

	public void setLikeInfo(String likeInfo) {
		this.likeInfo = likeInfo;
	}
}
