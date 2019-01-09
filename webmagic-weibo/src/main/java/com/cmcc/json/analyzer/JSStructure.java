package com.cmcc.json.analyzer;

import java.util.List;

public class JSStructure {
	private int count;
	private List<JSFieldInfo> fieldInfos;

	public JSStructure(List<JSFieldInfo> fieldInfos) {
		super();
		this.fieldInfos = fieldInfos;
	}

	public void articlePlus() {
		count++;
	}

	public List<JSFieldInfo> getFieldInfos() {
		return fieldInfos;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\n");
		sb.append(
				String.format("%1$-30s%2$-10s%3$-10s%4$-10s%5$-5s%6$s",
						("articleCount = " + count), "empty", "null",
						"exception", ">20", "possibleValue")).append('\n');
		for (JSFieldInfo fieldInfo : fieldInfos) {
			sb.append(fieldInfo.toRowString()).append('\n');
		}
		return sb.toString();
	}

}
