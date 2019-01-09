package com.cmcc.wltx.collector.spider.mywebmagic.pipeline;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * 什么也不做
 * @author Future
 *
 */
public class MyNullPipeline implements Pipeline {
	@Override
	public void process(ResultItems resultItems, Task task) {}
}
