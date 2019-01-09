package com.cmcc.wltx.collector.spider.util;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.http.client.utils.URIUtils;

import com.cmcc.wltx.common.MD5;
import com.cmcc.wltx.model.Article;

public class ModelUtils {
	public static Article convertArticle(com.cmcc.wltx.collector.spider.model.Article source, Date now) {
		Article target = new Article();
		target.setId(MD5.getMD5(source.getUrl()));
		Integer recType = source.getSourceType();
		target.setRecType(recType);

		String pubSource = source.getPubSource();
		if (Article.RECTYPE_WEIXIN == recType) {
			if (null != source.getCreateDate()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String sourceDate = sdf.format(source.getCreateDate());
				if (sourceDate.equals(sdf.format(now))) {
					source.setCreateDate(now);
				} else {
					try {
						source.setCreateDate(
								new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(sourceDate + "_23:59:59"));
					} catch (ParseException e) {
						throw new Error(e);
					}
				}
			}
			if (null != pubSource && pubSource.length() != 0) {
				source.setSiteName(pubSource);
			}
		}

		String host = null;
		try {
			host = URIUtils.extractHost(URI.create(source.getUrl())).getHostName();
		} catch (Exception e) {
		}
		String businessType = target.getBusinessType();
		if (null != businessType) {
			target.setBusinessType(businessType);
		}

		target.setReference(source.getUrl());
		target.setDate(now);
		target.setFfdCreate(null == source.getCreateDate() ? now : source.getCreateDate());

		String siteName = source.getSiteName();
		String channel = source.getChannel();
		if (null != siteName && siteName.length() != 0) {
			if (null != channel && channel.length() != 0) {
				target.setTheSource(siteName + ">" + channel);
			} else {
				target.setTheSource(siteName);
			}
		} else {
			if (null != channel && channel.length() != 0) {
				target.setTheSource(channel);
			} else {
				if (null != host && host.length() != 0) {
					target.setTheSource(host);
				}
			}
		}

		if (null != pubSource && pubSource.length() != 0) {
			target.setDreSource(pubSource);
		} else {
			String theSource = target.getTheSource();
			if (null != theSource && theSource.length() != 0) {
				target.setDreSource(theSource);
			}
		}

		String title = source.getTitle();
		if (null == title || title.length() == 0) {
			title = source.getListTitle();
			if (null == title || title.length() == 0) {
				title = source.getLongTitle();
			}
		}
		target.setTitle(title);
		target.setContent(source.getContent());
		target.setSummary(source.getSummary());
		Set<String> relatedUrls = source.getRelatedUrls();
		if (null != relatedUrls && relatedUrls.size() > 0) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String relatedUrl : relatedUrls) {
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				sb.append(relatedUrl);
			}
			target.setLikeInfo(sb.toString());
			target.setLikeInfoCount(relatedUrls.size());
		}
		List<String> authors = source.getAuthors();
		if (null != authors && authors.size() > 0) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String author : authors) {
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				sb.append(author);
			}
			target.setScreenName(sb.toString());
		} else {
			target.setScreenName(source.getAuthor());
		}
		target.setComments(source.getCommentsCount());
		target.setReportCount(source.getForwardCount());
		target.setReadCount(source.getClickCount());
		target.setMediaType(1);
		target.setArea(source.getArea());

		target.setLikeInfo(source.getLikeInfo());

		return target;
	}
}
