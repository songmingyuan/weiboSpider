package com.cmcc.wltx.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;

public class TimeFormatUtils {

	public static Timestamp parseStrToTimestamp(String dateStr){
		Date date = null;
		String reg = "[\u4e00-\u9fa5]+"; // 匹配汉字字符串
		dateStr= dateStr.replaceAll(reg, " ").trim();
		dateStr = dateStr.replaceAll("[a-zA-Z]", " ").trim();
		dateStr= dateStr.replaceAll("\\-|\\.|/", " ");
		
		dateStr = dateStr.replaceAll("\\D", " ");
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy MM dd HH mm ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy MM dd HH mm");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy MM dd");
		try {
			date = sdf1.parse(dateStr);
		} catch (ParseException e) {
			try {
				date = sdf2.parse(dateStr);
			} catch (ParseException e1) {
				try {
					date = sdf3.parse(dateStr);
				} catch (ParseException e2) {
					LoggerFactory.getLogger(TimeFormatUtils.class).info(dateStr+"时间格式转换错误");
				}
			}
			//e.printStackTrace();
		}
		if (null!=date)	
		{
			return new Timestamp(date.getTime());
		}else{
			return null;
		}
	}
	
	public static void main(String[] args) {
	}
}
		
