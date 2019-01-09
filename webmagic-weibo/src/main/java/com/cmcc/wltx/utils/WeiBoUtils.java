package com.cmcc.wltx.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author mingyuan.song
 *
 */
public class WeiBoUtils {

	/**
	 * 62位数组
	 */
	public static String[] str62keys = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f",
			"g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A",
			"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z" };

	/**
	 * 62进制转10进制
	 */
	public static long str62To10(String str62) {
		long i10 = 0;
		String[] array = strToArray(str62);
		for (int i = 0; i < array.length; i++) {
			long n = array.length - i - 1;
			String s = array[i];
			i10 += getStr62Index(s) * Math.pow(62, n);
		}
		return i10;
	}

	/**
	 * 10进制转62进制
	 */
	public static String str10To62(String str10) {
		String s62 = "";
		int mod = 0;
		int i10 = Integer.valueOf(str10);
		while (i10 != 0) {
			mod = i10 % 62;
			s62 = s62 + str62keys[mod];
			i10 = i10 / 62;
		}
		return s62;
	}

	/**
	 * 将string变为数组
	 */
	public static String[] strToArray(String str) {
		String[] s = new String[str.length()];
		for (int i = 0; i < str.length(); i++) {
			s[i] = str.substring(i, i + 1);
		}
		return s;
	}

	/**
	 * 字母在str62keys中 出现的位置
	 */
	public static int getStr62Index(String s) {
		int t = 0;
		for (int i = 0; i < str62keys.length; i++) {
			if (s.equals(str62keys[i])) {
				t = i;
			}
		}
		return t;
	}

	/**
	 * 字符串反转
	 * 
	 * @param str
	 * @return
	 */
	public static String reverseStr(String str) {
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * mid to sid
	 * 
	 * @param mid
	 * @return
	 */
	public static String midToSid(String mid) {
		String sid = "";
		mid = reverseStr(mid);
		for (int i = 0; i < mid.length(); i = i + 7) {
			int pos = i + 7;
			if (pos > mid.length()) {
				pos = mid.length();
			}
			String subStr = mid.substring(i, pos);
			subStr = reverseStr(subStr);
			String c = str10To62(subStr);
			while (c.length() < 4) {
				c = c + "0";
			}
			sid = sid + c;
		}
		sid = reverseStr(sid);
		while (sid.startsWith("0")) {
			sid = sid.substring(1);
		}
		return sid;
	}

	/**
	 * 获取微博原文链接地址
	 * 
	 * @param weiboId
	 *            账户id
	 * @param mid
	 *            微博正文id
	 * @return
	 */
	public static String getReference(String weiboId, String mid) {
		StringBuffer buffer = new StringBuffer();
		if (StringUtils.isNotBlank(weiboId) && StringUtils.isNotBlank(mid)) {
			buffer.append("http://weibo.com/").append(weiboId).append("/").append(midToSid(mid));
		}
		return buffer.toString();
	}
}
