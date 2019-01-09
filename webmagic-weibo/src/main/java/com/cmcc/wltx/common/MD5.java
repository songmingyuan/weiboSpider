package com.cmcc.wltx.common;

import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MD5.java MD5加密算法
 *
 * @author 
 * @email 
 */
public class MD5 {

	private static final Logger logger = LoggerFactory.getLogger(MD5.class);
	
    /**
     * 构造方法
     */
    public MD5() {
    }
    
    /**
     * 加密
     * @param str String
     * @return String
     */
    public final static String getMD5(String str) {
        /* 判断是否为null，是则返回 null */
        if (str == null) {
            return str;
        }

        char hexDigits[] = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd',
            'e', 'f'};

        byte[] strTemp = str.getBytes();
        MessageDigest md5Temp = null;
        try {
            md5Temp = MessageDigest.getInstance("md5");
        } catch (java.security.NoSuchAlgorithmException nsae) {
        	logger.info("Exception: " + nsae.toString());
            return null;
        }
        md5Temp.update(strTemp);

        byte[] md5 = md5Temp.digest();
        int md5Length = md5.length;
        char md5Str[] = new char[md5Length * 2];

        int j = 0;
        for (int i = 0; i < md5Length; i++) {
            byte byte0 = md5[i];
            md5Str[j++] = hexDigits[byte0 >>> 4 & 0xf];
            md5Str[j++] = hexDigits[byte0 & 0xf];
        }

        return new String(md5Str);
    }

}
