package com.cmcc.json.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.cmcc.json.JSException;
import com.cmcc.json.JSObject;

public class JSStructureAnalyzer {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(JSStructureAnalyzer.class);
	private final String KEYS_FILEPATH = "D:\\Future\\temp\\Log\\webmagic_my\\analysis\\keys_structure";
	private final JSStructure structure;
	private final Set<String> keys = new HashSet<String>();

	@SuppressWarnings("unchecked")
	public JSStructureAnalyzer() throws IOException {
		File file = new File(KEYS_FILEPATH);
		if (!file.isFile()) {
			structure = null;
			return;
		}
		List<String> lines = FileUtils.readLines(file);
		List<JSFieldInfo> fieldInfos = new ArrayList<JSFieldInfo>(lines.size());
		StringBuilder sb = new StringBuilder();
		for (String fieldName : lines) {
			JSFieldInfo fieldInfo = new JSFieldInfo();
			if (fieldName.startsWith("#")) {
				fieldName = fieldName.substring(1);
				fieldInfo.setLogField(true);
				sb.append(fieldName).append('\t');
			}
			if (keys.add(fieldName)) {
				fieldInfo.setFieldName(fieldName);
				fieldInfos.add(fieldInfo);
			}
		}
		if (sb.length() != 0) {
			sb.deleteCharAt(sb.length() - 1);
			logger.info(sb.toString());
		}
		this.structure = new JSStructure(fieldInfos);
	}

	private void saveKeys() throws IOException {
		List<String> lines = new ArrayList<String>(keys.size()); 
		if (null != structure) {
			List<JSFieldInfo> fieldInfos = this.structure.getFieldInfos();
			for (JSFieldInfo fi : fieldInfos) {
				String fieldName = fi.getFieldName();
				keys.remove(fieldName);
				if (fi.isLogField()) {
					fieldName = '#' +fieldName;
				}
				lines.add(fieldName);
			}
		}
		for (String key : keys) {
			lines.add(key);
		}
		File file = new File(KEYS_FILEPATH);
		FileUtils.writeLines(file, lines);
	}

	public void analyze(JSObject jso) {
		keys.addAll(jso.keySet());
		if (null == structure) {
			return;
		}
		structure.articlePlus();
		StringBuilder sb = new StringBuilder();
		for (JSFieldInfo fi : this.structure.getFieldInfos()) {
			String name = fi.getFieldName();
			try {
				Object obj = jso.get(name);
				if (null == obj) {
					fi.nullPlus();
					if (fi.isLogField()) {
						sb.append("null").append('\t');
					}
				} else {
					String value = obj.toString().trim();
					if (fi.isLogField()) {
						sb.append(value.replace("\n", "\\n").replace("\t", "\\t")).append('\t');
					}
					if (value.length() == 0) {
						fi.emptyPlus();
					} else {
						if (fi.getPossibleValue().size() == 20) {
							fi.setValueEnumerative(true);
						} else if (fi.getPossibleValue().size() < 20) {
							if (value.length() > 20
									&& !value.startsWith("http://")
									&& !value.startsWith("https://")) {
								value = value.substring(0, 20) + "...";
							}
							fi.getPossibleValue().add(value);
						}
					}
				}
			} catch (JSException e) {
				fi.expPlus();
				if (fi.isLogField()) {
					sb.append("undefined").append('\t');
				}
			}
		}
		if (sb.length() != 0) {
			sb.deleteCharAt(sb.length() - 1);
			logger.info(sb.toString());
		}
	}

	public void report() throws IOException {
		if (null != structure) {
			logger.info(this.structure.toString());
		}
		logger.info("keys.size() = {}", keys.size());
		saveKeys();
	}
}
