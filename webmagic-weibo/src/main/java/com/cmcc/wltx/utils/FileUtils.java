package com.cmcc.wltx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用工具类
 * 
 */
public final class FileUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * 路径末尾补分隔符
	 * 
	 * @param path
	 * @return
	 */
	public static String getWholePath(String path) {
		String wholePath = path != null ? ((path.endsWith("/") || path
				.endsWith("\\")) ? path : (path + File.separator)) : "";
		return wholePath;
	}

	/**
	 * 字符串多条目替换
	 * 
	 * @param oldStr
	 * @param map
	 * @return
	 */
	public static String getStringReplace(String oldStr, Map<String, String> map) {
		String newStr = oldStr;
		Collection<String> c = map.keySet();
		Iterator<String> ite = c.iterator();
		while (ite.hasNext()) {
			String keyStr = ite.next();
			String valueStr = map.get(keyStr);
			newStr = newStr.replace(keyStr, valueStr);
		}
		return newStr;
	}

	/**
	 * 文件拷贝 不支持压缩文件
	 * 
	 * @param source
	 *            源文件
	 * @param target
	 *            目标路径 不需要带文件名称
	 * @return
	 */
	
	public static boolean copyFile(String source, String target) {
		try {
			
			File file = new File(source);
			String tmpName = file.getName();
			file = new File(target);
			
			if (!file.exists()) {
				file.mkdirs();
			}
			
			file = null;
			System.gc();
			
			int length = 2097152;
			
			FileInputStream in = new FileInputStream(new File(source));
			FileOutputStream out = new FileOutputStream(new File(target + "/" + tmpName));
			FileChannel inC = in.getChannel();
			FileChannel outC = out.getChannel();
			ByteBuffer b = null;
			
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
			}
			
			if ((inC.size() - inC.position()) < length) {
				length = (int) (inC.size() - inC.position());
			} else {
				length = 2097152;
			}
			
			b = ByteBuffer.allocateDirect(length);
			inC.read(b);
			b.flip();
			outC.write(b);
			outC.force(false);
			outC.close();
			inC.close();
			
		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 文件拷贝 不支持压缩文件如果目录不存在不新建目录
	 * 
	 * @param source
	 *            源文件
	 * @param target
	 *            目标路径 不需要带文件名称
	 * @return
	 */
	public static boolean copyFile2(String source, String target) {
		try {
			File file = new File(source);
			String tmpName = file.getName();
			file = new File(target);
			if (!file.exists()) {
				return false;
			}
			file = null;
			System.gc();
			int length = 2097152;
			FileInputStream in = new FileInputStream(new File(source));
			FileOutputStream out = new FileOutputStream(new File(target + "/"
					+ tmpName));
			FileChannel inC = in.getChannel();
			FileChannel outC = out.getChannel();
			ByteBuffer b = null;
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
			}
			if ((inC.size() - inC.position()) < length) {
				length = (int) (inC.size() - inC.position());
			} else {
				length = 2097152;
			}
			b = ByteBuffer.allocateDirect(length);
			inC.read(b);
			b.flip();
			outC.write(b);
			outC.force(false);
			outC.close();
			inC.close();
		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 文件删除（JVM执行完毕后）
	 * 
	 * @param targetFileName
	 *            需要删除的文件路径及名称
	 * @return
	 */
	public static boolean deleteFileOnExit(String targetFileName) {
		try {
			File file = new File(targetFileName);
			file.deleteOnExit();
			file = null;
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 文件删除
	 * 
	 * @param targetFileName
	 *            需要删除的文件路径及名称（file.delete()，非file.deleteOnExit()）
	 * @return
	 */
	public static boolean deleteFileForRelease(String targetFileName) {
		try {
			File file = new File(targetFileName);
			file.delete();
			file = null;
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * 如果目标目录不存在则构建目录
	 */
	public static void mkDirs(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * GBK转ISO编码
	 * 
	 * @param str
	 * @return
	 */
	public static String gb2iso(String str) {
		if (str != null) {
			byte[] tmpbyte = null;
			try {
				tmpbyte = str.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				logger.warn("Error: Method: dbconn.gb2iso :"
						+ e.getMessage());
			}
			try {
				str = new String(tmpbyte, "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				logger.warn("Error: Method: dbconn.gb2iso :"
						+ e.getMessage());
			}
		}
		return str;
	}
	
	/**
	 * 当前时间前一小时yyyyMMddHH
	 * @return
	 */
	public static Long getLastHour(){
		return getLastHour(1);
	}
	
	public static Long getLastHour(int value){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -1 * value);
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
		String s =formatter.format(calendar.getTime());
		return Long.parseLong(s);
	}
	
	public static Integer getCurrentDate(){
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String s =formatter.format(date);
		return Integer.parseInt(s);
	}
	
	public static Integer getDateDiff(Date date,Integer value){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, value);
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String s =formatter.format(calendar.getTime());
		return Integer.parseInt(s);
	}
	
	public static Integer getDateDiff(Integer value){
		return getDateDiff(new Date(), value);
	}
	
	private static InetAddress getLocalHost(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return addr;
	}
	
	/**
	 * 获取本机IP
	 * @return
	 */
	public static String getHostAddress(){
		return getLocalHost().getHostAddress();
	}
	
	/**
	 * 获取本机计算机名
	 * @return
	 */
	public static String getHostName(){
		return getLocalHost().getHostName();
	}
	
	/**
	 * 格式化时间为yyyy-MM-dd HH:mm:ss
	 * @param timeStamp
	 * @return
	 */
	public static String fmtDate(Date date){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
	
	/**
	 * 检查字符串trim后是否为空
	 * @param str
	 */
	public static boolean isTrimEmpty(String str){
		if(str == null)
			return true;
		else
			return str.trim().length() == 0;
	}
	
		
	public static void main(String[] args) {
	}
	
}
