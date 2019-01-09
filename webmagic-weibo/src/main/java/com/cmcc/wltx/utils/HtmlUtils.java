package com.cmcc.wltx.utils;

import org.jsoup.Jsoup;

public class HtmlUtils {
	public static String removeHtmlTags(String htmlText){
		if (null == htmlText || htmlText.length() == 0) {
			return htmlText;
		}
		return Jsoup.parse(htmlText).text();
	}
}
