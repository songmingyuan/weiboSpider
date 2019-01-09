package com.cmcc.wltx.collector.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cmcc.jdbc.MyDataSource;
import com.cmcc.wltx.collector.dao.WeiboUserDao;
import com.cmcc.wltx.collector.exception.DataAccessException;
import com.cmcc.wltx.collector.model.WeiboUser;

public class JDBCWeiboUserDao extends JDBCBasicDao implements
		WeiboUserDao {
	@Override
	public void update(WeiboUser user, String tableSuffix) throws SQLException, DataAccessException {
		if (null == user.getNickName()) {
			throw new DataAccessException("null == nickName");
		}
		List<String> paras = new ArrayList<String>();
		StringBuilder sb = new StringBuilder("update ");
		if (null == tableSuffix) {
			sb.append("temp_relation_ship_detail");
		} else {
			sb.append("part_relation_ship_").append(tableSuffix);
		}
		// 昵称
		sb.append(" set value_screenName=?");
		paras.add(user.getNickName());
		// 关注数
		if (user.getFollowCount() != null) {
			sb.append(",friends_count=").append(user.getFollowCount());
		}
		// 粉丝数
		if (null != user.getFanCount()) {
			sb.append(",followers_count=").append(user.getFanCount());
		}
		// 微博数
		if (null != user.getFeedCount()) {
			sb.append(",weibo_count=").append(user.getFeedCount());
		}
		// 头像
		if (null != user.getAvatarUrl()) {
			sb.append(",header_url=?");
			paras.add(user.getAvatarUrl());
		}
		// 认证类型
		if (null != user.getVerifyType()) {
			sb.append(",verify_type=").append(user.getVerifyType());
		}
		// 认证信息
		if (null != user.getVerifyInfo()) {
			sb.append(",spec_description=?");
			paras.add(user.getVerifyInfo());
		}
		// 行业类别
		if (null != user.getIndustryCategory()) {
			sb.append(",industry_type=?");
			paras.add(user.getIndustryCategory());
		}
		// 地域
		if (null != user.getRegion()) {
			sb.append(",region=?");
			paras.add(user.getRegion());
		}
		// 简介
		if (null != user.getPinfo()) {
			sb.append(",synopsis=?");
			paras.add(user.getPinfo());
		}
		// 百科链接
		if (null != user.getBaikeUrl()) {
			sb.append(",baidu_description=?");
			paras.add(user.getBaikeUrl());
		}
		if (null == tableSuffix) {
			sb.append(",createTime=").append(System.currentTimeMillis()).append(",status=").append(user.getStatus());
		}
		sb.append(" where value_id=").append(user.getId());

		try (PreparedStatement stmt = MyDataSource.getCurrentConnection()
				.prepareStatement(sb.toString())) {
			int size = paras.size();
			for (int i = 0; i < size; i++) {
				stmt.setString(i+1, paras.get(i));
			}
			int res = stmt.executeUpdate();
			if (1 != res) {
				throw new DataAccessException("uid[" + user.getId() + "]不存在");
			}
		}
	}

	@Override
	public void updateStatusById(int status, long id) throws SQLException,
			DataAccessException {
		if (1 != executeUpdate("update temp_relation_ship_detail set status="
				+ status + " where value_id='" + id + '\'')) {
			throw new DataAccessException("uid[" + id + "]不存在");
		}
	}

}
