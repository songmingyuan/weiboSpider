package com.cmcc.wltx.collector.spider.mywebmagic.pipeline;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;

import com.cmcc.wltx.collector.spider.model.Article;
import com.cmcc.wltx.collector.spider.util.ModelUtils;
import com.cmcc.wltx.common.VerticalOutFileWriter;

/**
 * 使用公共的OutFileWriter 一页多个article
 * 
 * @author Future
 *
 */
public class ArticlesOutFileWriterPipeline extends ArticleOutFileWriterPipeline {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ArticlesOutFileWriterPipeline.class);
	public static final String KEY_ARTICLES = "articles";

	public ArticlesOutFileWriterPipeline(String filePipelinePath, boolean countSiteTarget, int limit) {
		super(filePipelinePath, countSiteTarget, limit);
	}

	/**
	 * article写out文件(原来)
	 * 
	 * @param resultItems
	 * @param task
	 */
	public void process_old(ResultItems resultItems, Task task) {
		@SuppressWarnings("unchecked")
		List<Article> articles = (List<Article>) resultItems.get(KEY_ARTICLES);
		if (null == articles) {
			return;
		}
		int size = articles.size();
		if (size == 0) {
			return;
		}

		logger.info("pipe start - {}", resultItems.getRequest().getUrl());
		Date now = new Date();
		for (Article article : articles) {
			if (null == article) {
				continue;
			}
			VerticalOutFileWriter ofw = getOutFileWriter(article.getSourceType());
			try {
				ofw.writeArticle(ModelUtils.convertArticle(article, now));
				logger.info("pipe - {}", article.getUrl());
			} catch (IOException e) {
				String url = article.getUrl();
				logger.warn(url + "写入文件失败", e);
			}
		}
		logger.info("pipe done - {}", resultItems.getRequest().getUrl());
	}

	/**
	 * article写kafka(现在)
	 * 
	 * @param resultItems
	 * @param task
	 */
	@Override
	public void process(ResultItems resultItems, Task task) {
		@SuppressWarnings("unchecked")
		List<Article> articles = (List<Article>) resultItems.get(KEY_ARTICLES);
		if (null == articles) {
			return;
		}
		int size = articles.size();
		if (size == 0) {
			return;
		}

		logger.info("pipe start - {}", resultItems.getRequest().getUrl());
		Date now = new Date();
		for (Article article : articles) {
			if (null == article) {
				continue;
			}
			VerticalOutFileWriter ofw = getOutFileWriter(article.getSourceType());
			try {
				ofw.writeArticleToKafka(ModelUtils.convertArticle(article, now));
				logger.info("pipe - {}", article.getUrl());
			} catch (IOException e) {
				String url = article.getUrl();
				logger.warn(url + "写入kafka失败", e);
			}
		}
		logger.info("pipe done - {}", resultItems.getRequest().getUrl());
	}

}
