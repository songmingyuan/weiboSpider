package com.cmcc.wltx.collector.spider.mywebmagic.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.spider.model.Article;
import com.cmcc.wltx.collector.spider.util.ModelUtils;
import com.cmcc.wltx.common.VerticalOutFileWriter;

/**
 * 使用公共的OutFileWriter 一页一个article
 * 
 * @author Future
 *
 */
public class ArticleOutFileWriterPipeline implements Pipeline {
	public static final String KEY_ARTICLE = "article";
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ArticleOutFileWriterPipeline.class);
	private final org.slf4j.Logger LOGGER_TARGET;
	private final String pipePath;
	private final int limit;

	public ArticleOutFileWriterPipeline(String filePipelinePath, boolean countSiteTarget) {
		this(filePipelinePath, countSiteTarget, 100);
	}

	public ArticleOutFileWriterPipeline(String filePipelinePath, boolean countSiteTarget, int limit) {
		super();
		if (countSiteTarget) {
			LOGGER_TARGET = org.slf4j.LoggerFactory.getLogger("com.cmcc.wltx.collector.pageprocessor");
		} else {
			LOGGER_TARGET = null;
		}
		File outPath = null;
		if (null == filePipelinePath || filePipelinePath.length() == 0) {
			outPath = new File(ConstantsHome.USER_DIR + File.separator + "out");
		} else {
			File file = new File(filePipelinePath);
			if (file.isAbsolute()) {
				outPath = file;
			} else {
				outPath = new File(ConstantsHome.USER_DIR, filePipelinePath);
			}
		}
		if (!outPath.exists() || !outPath.isDirectory()) {
			if (!outPath.mkdirs()) {
				throw new Error("创建输出目录失败 - " + outPath.getAbsolutePath());
			}
		}
		pipePath = outPath.getAbsolutePath();

		this.limit = limit;
	}

	@Override
	public void process(ResultItems resultItems, Task task) {
		logger.info("pipe start - {}", resultItems.getRequest().getUrl());

		Article article = (Article) resultItems.get(KEY_ARTICLE);
		if (null == article) {
			return;
		}

		VerticalOutFileWriter ofw = getOutFileWriter(article.getSourceType());

		try {
			ofw.writeArticle(ModelUtils.convertArticle(article, new Date()));
			logger.info("pipe done - {}", resultItems.getRequest().getUrl());
		} catch (IOException e) {
			String url = article.getUrl();
			logger.warn(url + "写入文件失败", e);
			if (null != LOGGER_TARGET) {
				Object siteTaskId = resultItems.get(ConstantsHome.REQUEST_EXTRA_SITE_TASK_ID);
				if (null != siteTaskId) {
					LOGGER_TARGET.info("{}\t{}\t{}\t{}\t{}\t{}\t{}", siteTaskId, 0, 0, 0, 0, 0, 1);
				}
			}
		}
	}

	protected VerticalOutFileWriter getOutFileWriter(int sourceType) {
		return VerticalOutFileWriter.getInstance(pipePath, sourceType, limit);
	}

}
