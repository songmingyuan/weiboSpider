package com.cmcc.wltx.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmcc.wltx.model.Article;

public class OutFileReader {
	
	private static final Logger logger = LoggerFactory.getLogger(OutFileReader.class);
	
	public static List<Article> getArticlesFromFile(File outFile){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile), "UTF-8"));
			String line = null;
			List<Article> list = new ArrayList<Article>();
			Article article = null;
			while((line = reader.readLine()) != null){
				if(StringUtils.isEmpty(line))
					continue;
				if(line.startsWith("#")){
					//老格式的out文件，以#BEGINDOC开头，#ENDDOC结束作为一条数据
					if(line.equals("#BEGINDOC")){
						article = new Article();
						String path = outFile.getPath();
						setProperty(article, "PATH", path);
						continue;
					}
					if(line.equals("#ENDDOC")){
						if(StringUtils.isEmpty(article.getId()) && !StringUtils.isEmpty(article.getReference())){
							article.setId(MD5.getMD5(article.getReference()));
						}
						list.add(article);
					}
					if(!line.contains("="))
						continue;
					
					int split = line.indexOf("=");
					String key = line.substring(0,split).trim();
					
					if(key.startsWith("#")){
						key = key.substring(1);
					}
					String value = line.substring(split + 1).trim();
					setProperty(article,key,value);
				}else{
					//以竖线分隔形式存储的out文件
					if("NULL\t".equals(line)){
						logger.info(outFile.getName()+" line data exception");
						continue;
					}
					list.add(VerticalLineOutFileReader.readLine(line));
				}
			}
			reader.close();
			return list;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void setProperty(Article article,String key,String value){
		if("ID".equals(key)){
			article.setId(value);
		}else	if("RECTYPE".equals(key)){
			int recType = convertToInt(value);
			article.setRecType(recType);
		}else if("THESOURCE".equals(key)){
			article.setTheSource(value);
		}else if("REFERENCE".equals(key)){
			article.setReference(value);
		}else if("DATE".equals(key)){
			Date date = convertDateStampToDate(value);
			article.setDate(date);
		}else if("FFDCREATE".equals(key)){
			Date date = convertDateStampToDate(value);
			article.setFfdCreate(date);
		}else if("DRESOURCE".equals(key)){
			article.setDreSource(value);
		}else if("TITLE".equals(key)){
			article.setTitle(value);
		}else if("CONTENT".equals(key)){
			article.setContent(value);
		}else if("ABSTRACT".equals(key)){
			article.setSummary(value);
		}else if("RECEMOTIONAL".equals(key)){
			int recEmotional = convertToInt(value);
			article.setRecEmotional(recEmotional);
		}else if("AREA".equals(key)){
			article.setArea(value);
		}else if("FREQUENCYWORD".equals(key)){
			article.setFrequencyWord(value);
		}else if("LIKEINFO".equals(key)){
			article.setLikeInfo(value);
		}else if("LIKEINFOCOUNT".equals(key)){
			int likeInfoCount = convertToInt(value);
			article.setLikeInfoCount(likeInfoCount);
		}else if("SCREEN_NAME".equals(key)){
			article.setScreenName(value);
		}else if("COMMENTS".equals(key)){
			int comments = convertToInt(value);
			article.setComments(comments);
		}else if("REPORTCOUNT".equals(key)){
			int reportCount = convertToInt(value);
			article.setReportCount(reportCount);
		}else if("READCOUNT".equals(key)){
			int readCount = convertToInt(value);
			article.setReadCount(readCount);
		}else if("WEIBOTYPE".equals(key)){
			int weiboType = convertToInt(value);
			article.setWeiboType(weiboType);
		}else if("WEIXINTYPE".equals(key)){
			int weixinType = convertToInt(value);
			article.setWeixinType(weixinType);
		}else if("HOTVALUE".equals(key)){
			int hotValue = convertToInt(value);
			article.setHotValue(hotValue);
		}else if("MEDIATYPE".equals(key)){
			int mediaType = convertToInt(value);
			article.setMediaType(mediaType);
		}else if("KEYWORD".equals(key)){
			article.setKeyWord(value);
		}else if("ALARMLEVEL".equals(key)){
			int alarmLevel = convertToInt(value);
			article.setAlarmLevel(alarmLevel);
		}
	}
	
	private static Date convertDateStampToDate(String dateStampStr){
		if(StringUtils.isEmpty(dateStampStr))
			return new Date(0);
		Long dateStamp = Long.parseLong(dateStampStr) * 1000;
		Date date = new Date(dateStamp);
		return date;
	}
	
	private static int convertToInt(String intStr){
		if(StringUtils.isEmpty(intStr))
			return 0;
		return Integer.parseInt(intStr);
	}
}
