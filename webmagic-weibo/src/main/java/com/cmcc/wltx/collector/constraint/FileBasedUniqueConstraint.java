package com.cmcc.wltx.collector.constraint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.cmcc.wltx.collector.ConstantsHome;

public class FileBasedUniqueConstraint implements UniqueConstraint {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FileBasedUniqueConstraint.class);
	private final Set<String> ids = new HashSet<String>();

	public FileBasedUniqueConstraint() throws IOException {
		this(ConstantsHome.USER_DIR + File.separatorChar + "ids");
	}

	public FileBasedUniqueConstraint(String path) throws IOException {
		super();
		File file = new File(path);
		if (!file.isFile()) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				ids.add(line);
			}
		}
	}

	@Override
	public boolean check(String id) {
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("id不能为空");
		}
		if (ids.add(id)) {
			logger.info(id);
			return true;
		}
		return false;
	}

	@Override
	public boolean checkSJ(String id) {
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("id不能为空");
		}
		if (ids.add(id)) {
			logger.info(id);
			return true;
		}
		return false;
	}

	@Override
	public boolean inspect(String key) {
		if (null == key || key.length() == 0) {
			throw new IllegalArgumentException("key不能为空");
		}
		if (ids.add(key)) {
			logger.info(key);
			return true;
		}
		return false;
	}
}
