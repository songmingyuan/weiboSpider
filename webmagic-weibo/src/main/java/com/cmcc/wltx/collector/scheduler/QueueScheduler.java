package com.cmcc.wltx.collector.scheduler;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.Scheduler;

public class QueueScheduler implements Scheduler {

    private Queue<Request> queue = new LinkedBlockingQueue<Request>();

    @Override
    public int push(Request request, Task task) {
    	try {
    		if (queue.add(request)) {
    			return PUSH_SUCCESS;
    		} else {
    			return PUSH_LIMITED;
    		}
		} catch (Exception e) {
			return PUSH_FAILED;
		}
    }

    @Override
    public Request poll(Task task) {
        return queue.poll();
    }
}
