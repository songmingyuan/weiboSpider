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

import com.cmcc.wltx.model.Article;

/**
 * 树线分隔格式的out文件阅读器，字段顺序如下
 * <pre>
 * RECNO|ID|RECTYPE|THESOURCE|REFERENCE|DATE|FFDCREATE|LANGUAGETYPE|DRESOURCE|TITLE|CONTENT|ABSTRACT|RECEMOTIONAL|AREA|FREQUENCYWORD|LIKEINFO|LIKEINFOCOUNT|SCREEN_NAME|COMMENTS|REPORTCOUNT|READCOUNT|WEIBOTYPE|WEIXINTYPE|HOTVALUE|MEDIATYPE|ALARMLEVEL|KEYWORD|BUSINESSTYPE|#
 * </pre>
 * @author liping
 *
 */
public class VerticalLineOutFileReader {
	public static List<Article> getArticlesFromFile(File outFile){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile), "UTF-8"));
			String line = null;
			List<Article> list = new ArrayList<Article>();
			while((line = reader.readLine()) != null){
				if(StringUtils.isEmpty(line))
					continue;
				list.add(readLine(line));
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
	
	/**
	 * 根据读取的一行字符串构建article对象，字段顺序
	 * <pre>
     * 0	RECNO
     * 1	ID
     * 2	RECTYPE
     * 3	THESOURCE
     * 4	REFERENCE
     * 5	DATE
     * 6	FFDCREATE
     * 7	LANGUAGETYPE
     * 8	DRESOURCE
     * 9	TITLE
     * 10	CONTENT
     * 11	ABSTRACT
     * 12	RECEMOTIONAL
     * 13	AREA
     * 14	FREQUENCYWORD
     * 15	LIKEINFO
     * 16	LIKEINFOCOUNT
     * 17	SCREEN_NAME
     * 18	COMMENTS
     * 19	REPORTCOUNT
     * 20	READCOUNT
     * 21	WEIBOTYPE
     * 22	WEIXINTYPE
     * 23	HOTVALUE
     * 24	MEDIATYPE
     * 25	ALARMLEVEL
     * 26	KEYWORD
     * 27	BUSINESSTYPE
     * 28   #
	 * </pre>
	 * @param line
	 * @return
	 */
	public static Article readLine(String line){
		String[] fields = line.split("\\|");
		if(fields == null)
			return null;
		Article article = new Article();
		article.setId(fields[1]);
		article.setRecType(convertToInt(fields[2]));
		article.setTheSource(unescape(fields[3]));
		article.setReference(fields[4]);
		article.setDate(convertDateStampToDate(fields[5]));
		article.setFfdCreate(convertDateStampToDate(fields[6]));
		article.setDreSource(unescape(fields[8]));
		article.setTitle(unescape(fields[9]));
		article.setContent(unescape(fields[10]));
		article.setSummary(unescape(fields[11]));
		article.setRecEmotional(convertToInt(fields[12]));
		article.setArea(unescape(fields[13]));
		article.setFrequencyWord(fields[14]);
		article.setLikeInfo(fields[15]);
		article.setLikeInfoCount(convertToInt(fields[16]));
		article.setScreenName(unescape(fields[17]));
		article.setComments(convertToInt(fields[18]));
		article.setReportCount(convertToInt(fields[19]));
		article.setReadCount(convertToInt(fields[20]));
		article.setWeiboType(convertToInt(fields[21]));
		article.setWeixinType(convertToInt(fields[22]));
		article.setHotValue(convertToInt(fields[23]));
		article.setMediaType(convertToInt(fields[24]));
		article.setAlarmLevel(convertToInt(fields[25]));
		article.setKeyWord(fields[26]);
		
		//老的数据格式 没有 BUSINESSTYPE 字段；这里为了兼容老的数据格式加这个判断
		if(fields.length==29){
			article.setBusinessType(fields[27]);
		}
		
		return article;
	}
	
	/**
	 * 过滤null值
	 * @param src
	 * @return
	 */
	private static String filterNull(Object src){
		if(src == null)
			return "";
		return src.toString();
	}

	/**
	 * 对字符串中的换行符、竖线进行反转义
	 * @param escape
	 * @return
	 */
	private static String unescape(String escape){
		if(StringUtils.isEmpty(escape))
			return filterNull(escape);
		return escape.replace("&#114;","\r").replace("&#110;","\n").replace("&#124;","|");
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
	
	public static void main(String[] args){
		File outFile = new File("E:\\111\\20160616190001_weixinspider.out");
		List<Article> list = VerticalLineOutFileReader.getArticlesFromFile(outFile);
	}
}
