package com.cmcc.wltx.collector.constraint;

public interface UniqueConstraint {
	/**
	 * 检查ID是否唯一
	 * 
	 * @param id
	 * @return id唯一时返回true，反之返回false
	 */
	boolean check(String id);

	boolean inspect(String key);
	
	boolean checkSJ(String id);
}
