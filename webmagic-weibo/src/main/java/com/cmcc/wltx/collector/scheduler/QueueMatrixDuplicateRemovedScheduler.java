package com.cmcc.wltx.collector.scheduler;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import com.cmcc.wltx.collector.ConstantsHome;
import com.cmcc.wltx.collector.spider.util.ProcessUtils;

public class QueueMatrixDuplicateRemovedScheduler extends DuplicateRemovedScheduler {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(QueueMatrixDuplicateRemovedScheduler.class);
	private final int pollTotalLimit;
	private volatile int index = 0;
	private final List<AtomicInteger> pollCounts;
	private final List<List<Entry<String, Queue<Request>>>> queueMatrix;

	public QueueMatrixDuplicateRemovedScheduler(DuplicateRemover duplicatedRemover, int queueMatrixSize,
			int pollTotalLimit) {
		super(duplicatedRemover);
		queueMatrix = new Vector<List<Entry<String,Queue<Request>>>>(queueMatrixSize);
		for (int i = 0; i < queueMatrixSize; i++) {
			queueMatrix.add(new Vector<Map.Entry<String,Queue<Request>>>());
		}
		this.pollTotalLimit = pollTotalLimit;
		if (pollTotalLimit > 0) {
			pollCounts = new Vector<AtomicInteger>();
		} else {
			pollCounts = null;
		}
	}

	@Override
	public int pushWhenNoDuplicate(Request request, Task task) {
		Object extra = request.getExtra(ConstantsHome.REQUEST_EXTRA_DEEP);
		int deep = null == extra?0:(Integer)extra;
		String url = request.getUrl();
		String domain = ProcessUtils.extractDomain(url);
		if (null == domain) {
			logger.warn("extract domain failed - {}", request.getUrl());
			return PUSH_FAILED;
		}
		Queue<Request> queue = null;
		
		synchronized (this) {
			int size = queueMatrix.size();
			if (deep >= size) {
				deep = size - 1;
			}
			List<Entry<String, Queue<Request>>> queues = queueMatrix.get(deep);
			
			int tindex = 0;
			for (int i = 0; i < queues.size(); i++) {
				tindex = i;
				Entry<String, Queue<Request>> entry = queues.get(i);
				if (domain.equals(entry.getKey())) {
					queue = entry.getValue();
					break;
				}
			}
			
			if (null == queue) {// 新的站点
				if (pollTotalLimit > 0) {
					pollCounts.add(new AtomicInteger(0));
				}
				for (List<Entry<String, Queue<Request>>> qs : queueMatrix) {
					if (queues == qs) {
						queue = new LinkedBlockingQueue<Request>();
						qs.add(new SimpleEntry<String, Queue<Request>>(domain, queue));
					} else {
						qs.add(new SimpleEntry<String, Queue<Request>>(domain, new LinkedBlockingQueue<Request>()));
					}
				}
			} else {
				if (pollTotalLimit > 0 && pollCounts.get(tindex).get() >= pollTotalLimit) {
					return PUSH_LIMITED;
				}
			}
			
			if (queue.add(request)) {
				return PUSH_SUCCESS;
			} else {
				return PUSH_FAILED;
			}
		}
	}

	@Override
	public synchronized Request poll(Task task) {
		int size = queueMatrix.size();
		if (size == 0) {
			return null;
		}
		size = queueMatrix.get(0).size();
		if (size == 0) {
			return null;
		}
		
		int ci = index;
		int maxIndex = size - 1;
		Request request = null;
		
		while (request == null) {
			if (pollTotalLimit <= 0 || pollCounts.get(index).get() < pollTotalLimit) {
				boolean reachLimit = false;
				for (List<Entry<String, Queue<Request>>> queues : queueMatrix) {
					Queue<Request> queue = queues.get(index).getValue();
					if (reachLimit) {
						queue.clear();
						continue;
					}
					request = queue.poll();
					if (null != request) {
						if (pollTotalLimit > 0 && pollCounts.get(index).incrementAndGet() >= pollTotalLimit){
							reachLimit = true;
							queue.clear();
							logger.info("last poll before limit - {}", request.getUrl());
						} else {
							break;
						}
					}
				}
			}
			
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
}
