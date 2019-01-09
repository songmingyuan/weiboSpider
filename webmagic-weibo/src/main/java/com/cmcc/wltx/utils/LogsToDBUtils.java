package com.cmcc.wltx.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmcc.wltx.common.LogsType;
import com.cmcc.wltx.database.DataBaseOperator;

/**
 * @author mingyuan.song
 * 
 *         通用日志入库操作类
 */
public class LogsToDBUtils {
	private static final Logger logger = LoggerFactory.getLogger(LogsToDBUtils.class);
	private static final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * @param paramList
	 * @param logsType
	 */
	public static void setLogsToDB(List<Map<String, Object>> paramList, LogsType logsType) {
		logger.info("setLogsToDB is start");
		DataBaseOperator db = new DataBaseOperator();
		if (logsType.equals(LogsType.OPERATION)) {
			setOperationLogsToDB(paramList, db);
		} else if (logsType.equals(LogsType.RUNNING)) {
			setRunningLogsToDB(paramList, db);
		}
		db.close();
		logger.info("setLogsToDB is end");
	}

	/**
	 * 写操作日志入库
	 * 
	 * @param paramList
	 *            projectID：工程名称； model：模块名称； resID：对应id； content：存储内容；
	 *            userID：用户ID
	 * @param db
	 */
	public static void setOperationLogsToDB(List<Map<String, Object>> paramList, DataBaseOperator db) {
		StringBuffer buffer = new StringBuffer();
		Date createDate = new Date();
		buffer.append(
				"insert into operation_log_data (projectID,model,resID,content,userID,createDate,updateTime) values ");
		if (paramList != null && paramList.size() > 0) {
			try {
				int num = 0;
				for (Map<String, Object> detail : paramList) {
					buffer.append("(");
					buffer.append(escapeString(detail.get("projectID"))).append(",");
					buffer.append(escapeString(detail.get("model"))).append(",");
					buffer.append(escapeString(detail.get("resID"))).append(",");
					buffer.append(escapeString(detail.get("content"))).append(",");
					buffer.append(escapeString(detail.get("userID"))).append(",");
					buffer.append(escapeString(dateFormater.format(createDate))).append(",");
					buffer.append(escapeString(sdf.format(createDate))).append(")");
					if (num < paramList.size() - 1) {
						buffer.append(",");
					} else {
						buffer.append(";");
					}
					num++;
				}
				String sql = buffer.toString();
				db.execute(sql, null);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("setOperationLogsToDB is error");
			}
		}
	}

	/**
	 * 写操作日志入库
	 * 
	 * @param paramList
	 *            projectID：工程名称； model：模块名称； resID：对应id； content：存储内容；
	 *            resIP：存放访问ip；operateType：存放操作类型； userID：用户ID
	 * @param db
	 */
	public static void setOperationLogsToDB(List<Object[]> paramList) {
		DataBaseOperator db = new DataBaseOperator();
		logger.info("setOperationLogsToDB 日志入库start：" + System.currentTimeMillis());
		List<Object[]> resultList = null;
		Date createDate = new Date();
		String sql = "insert into operation_log_data (projectID,model,resID,content,resIP,operateType,userID,createDate,updateTime) values (?,?,?,?,?,?,?,?,?)";
		if (paramList != null && paramList.size() > 0) {
			try {
				resultList = new ArrayList<Object[]>();
				int resultNum = 0;
				Object[] resultParam = null;
				for (Object[] detail : paramList) {
					resultNum = detail.length + 2;
					resultParam = new Object[resultNum];
					for (int num = 0; num < detail.length; num++) {
						resultParam[num] = detail[num];
					}
					resultParam[detail.length] = dateFormater.format(createDate);
					resultParam[detail.length + 1] = sdf.format(createDate);
					resultList.add(resultParam);
				}
				db.execBatch(sql, resultList);
				logger.info("setOperationLogsToDB 日志入库stop：" + System.currentTimeMillis());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("setOperationLogsToDB is error");
			}
		}
		db.close();
	}

	/**
	 * 写运行日志入库
	 * 
	 * @param paramList
	 *            projectID：工程名称； model：模块名称； resID：对应id；
	 *            content：存储内容；resIP：存放访问ip；operateType：存放操作类型；
	 * @param db
	 */
	public static void setRunningLogsToDB(List<Object[]> paramList) {
		DataBaseOperator db = new DataBaseOperator();
		logger.info("setRunningLogsToDB 日志入库start：" + System.currentTimeMillis());
		List<Object[]> resultList = null;
		Date createDate = new Date();
		String sql = "insert into running_log_data (projectID,model,resID,content,resIP,operateType,createDate,updateTime) values (?,?,?,?,?,?,?,?)";
		if (paramList != null && paramList.size() > 0) {
			try {
				resultList = new ArrayList<Object[]>();
				int resultNum = 0;
				Object[] resultParam = null;
				for (Object[] detail : paramList) {
					resultNum = detail.length + 2;
					resultParam = new Object[resultNum];
					for (int num = 0; num < detail.length; num++) {
						resultParam[num] = detail[num];
					}
					resultParam[detail.length] = dateFormater.format(createDate);
					resultParam[detail.length + 1] = sdf.format(createDate);
					resultList.add(resultParam);
				}
				db.execBatch(sql, resultList);
				logger.info("setRunningLogsToDB 日志入库stop：" + System.currentTimeMillis());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("setRunningLogsToDB is error");
			}
		}
		db.close();
	}

	/**
	 * 写运行日志入库
	 * 
	 * @param paramList
	 *            projectID：工程名称； model：模块名称； resID：对应id； content：存储内容；
	 * @param db
	 */
	public static void setRunningLogsToDB(List<Map<String, Object>> paramList, DataBaseOperator db) {
		logger.info("setRunningLogsToDB 运行日志入库开始时间：" + System.currentTimeMillis());
		StringBuffer buffer = new StringBuffer();
		Date createDate = new Date();
		buffer.append("insert into running_log_data (projectID,model,resID,content,createDate,updateTime) values ");
		if (paramList != null && paramList.size() > 0) {
			try {
				int num = 0;
				for (Map<String, Object> detail : paramList) {
					buffer.append("(");
					buffer.append(escapeString(detail.get("projectID"))).append(",");
					buffer.append(escapeString(detail.get("model"))).append(",");
					buffer.append(escapeString(detail.get("resID"))).append(",");
					buffer.append(escapeString(detail.get("content"))).append(",");
					buffer.append(escapeString(dateFormater.format(createDate))).append(",");
					buffer.append(escapeString(sdf.format(createDate))).append(")");
					if (num < paramList.size() - 1) {
						buffer.append(",");
					} else {
						buffer.append(";");
					}
					num++;
				}
				String sql = buffer.toString();
				db.execute(sql, null);
				logger.info("setRunningLogsToDB 运行日志入库结束时间：" + System.currentTimeMillis());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("setRunningLogsToDB is error");
			}
		}
	}

	/**
	 * 转译参数值
	 * 
	 * @param originalParam
	 * @return
	 */
	public static String escapeString(Object originalParam) {
		String result = null;
		if (originalParam != null) {
			String tempParams = String.valueOf(originalParam);
			if (StringUtils.isNotBlank(tempParams)) {
				result = "'" + originalParam + "'";
			}
		}
		return result;
	}
}
