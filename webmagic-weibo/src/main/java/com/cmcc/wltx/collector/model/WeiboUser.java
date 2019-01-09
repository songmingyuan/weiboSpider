package com.cmcc.wltx.collector.model;

public class WeiboUser {
	public static final int STATUS_NORMAL = 1;
	public static final int STATUS_404 = -1;
	public static final int STATUS_NOTEXIST = -2;
	public static final int TYPE_VERIFY_NONE = 0;
	public static final int TYPE_VERIFY_BLUE = 1;
	public static final int TYPE_VERIFY_YELLOW = 2;
	public static final int TYPE_VERIFY_GOLD = 3;
	public static final int TYPE_VERIFY_CLUB = 4;
	public static final int TYPE_VERIFY_LADY = 5;
	private Long id;
	/**
	 * 昵称
	 */
	private String nickName;
	/**
	 * 关注数
	 */
	private Long followCount;
	/**
	 * 粉丝数
	 */
	private Long fanCount;
	/**
	 * 微博数
	 */
	private Long feedCount;
	/**
	 * 头像URL
	 */
	private String avatarUrl;
	/**
	 * 认证类别
	 */
	private Integer verifyType;
	/**
	 * 认证信息
	 */
	private String verifyInfo;
	/**
	 * 行业分类
	 */
	private String industryCategory;
	/**
	 * 地域
	 */
	private String region;
	/**
	 * 简介
	 */
	private String pinfo;
	/**
	 * 百科链接
	 */
	private String baikeUrl;
	/**
	 * 1：正常；-1：404异常；-2：可能不存在
	 */
	private int status;

	public WeiboUser() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Long getFollowCount() {
		return followCount;
	}

	public void setFollowCount(Long followCount) {
		this.followCount = followCount;
	}

	public Long getFanCount() {
		return fanCount;
	}

	public void setFanCount(Long fanCount) {
		this.fanCount = fanCount;
	}

	public Long getFeedCount() {
		return feedCount;
	}

	public void setFeedCount(Long feedCount) {
		this.feedCount = feedCount;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Integer getVerifyType() {
		return verifyType;
	}

	public void setVerifyType(Integer verifyType) {
		this.verifyType = verifyType;
	}

	public String getVerifyInfo() {
		return verifyInfo;
	}

	public void setVerifyInfo(String verifyInfo) {
		this.verifyInfo = verifyInfo;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPinfo() {
		return pinfo;
	}

	public void setPinfo(String pinfo) {
		this.pinfo = pinfo;
	}

	public String getBaikeUrl() {
		return baikeUrl;
	}

	public void setBaikeUrl(String baikeUrl) {
		this.baikeUrl = baikeUrl;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "WeiboUser [id=" + id + ", nickName=" + nickName
				+ ", followCount=" + followCount + ", fanCount=" + fanCount
				+ ", feedCount=" + feedCount + ", avatarUrl=" + avatarUrl
				+ ", verifyType=" + verifyType + ", info=" + verifyInfo
				+ ", industryCategory=" + industryCategory + ", region="
				+ region + ", pinfo=" + pinfo + ", baikeUrl=" + baikeUrl
				+ ", status=" + status + "]";
	}

}
