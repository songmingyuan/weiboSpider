package com.cmcc.wltx.collector.spider.mywebmagic.scheduler;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.cmcc.wltx.collector.spider.util.ProcessUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;

/**
 * 模板测试工具和目录页爬虫使用。多队列，不会记录urls.txt文件
 * @author Future
 *
 */
public class QueuesScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler {

	private volatile int index = 0;
    private Vector<Entry<String, Queue<Request>>> queues = new Vector<Entry<String, Queue<Request>>>();

    @Override
    public boolean pushWhenNoDuplicate(Request request, Task task) {
    	if (null == request) {
			return false;
		}
		String url = request.getUrl();
		String domain = ProcessUtils.extractDomain(url);
		Queue<Request> queue = null;
		synchronized (this) {
			for (Entry<String, Queue<Request>> entry : queues) {
				if (domain.equals(entry.getKey())) {
					queue = entry.getValue();
					break;
				}
			}
    		if (null == queue) {
    			queue = new LinkedBlockingQueue<Request>();
    			queues.add(new SimpleEntry<String, Queue<Request>>(domain, queue));
			}
		}
		return queue.add(request);
    }

    @Override
    public synchronized Request poll(Task task) {
    	int size = queues.size();
		if (size == 0) {
			return null;
		}
    	int ci = index;
    	int maxIndex = size - 1;
    	Request request = null;
    	
    	while (request == null) {
    		Queue<Request> queue = queues.get(index).getValue();
    		request = queue.poll();
    		if (index >= maxIndex) {
    			index = 0;
    		} else {
    			index++;
    		}
    		if (index == ci) {
    			break;
    		}
		}
        return request;
    }

    @Override
    public int getLeftRequestsCount(Task task) {
		int count = 0;
		synchronized (this) {
			for (Entry<String, Queue<Request>> entry : queues) {
				count += entry.getValue().size();
			}
		}
		return count;
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return getDuplicateRemover().getTotalRequestsCount(task);
    }
    
    public synchronized void clear(){
		for (Entry<String, Queue<Request>> entry : queues) {
			entry.getValue().clear();
		}
    }
}
