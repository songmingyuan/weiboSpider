package com.cmcc.wltx.collector.scheduler;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

public abstract class DuplicateRemovedScheduler implements Scheduler {
    private final DuplicateRemover duplicatedRemover;

    public DuplicateRemovedScheduler(DuplicateRemover duplicatedRemover) {
		super();
		this.duplicatedRemover = duplicatedRemover;
	}

    @Override
	public int push(Request request, Task task) {
		if (null != request.getExtra(Request.CYCLE_TRIED_TIMES)
				|| null == duplicatedRemover
				|| !duplicatedRemover.isDuplicate(request, task)) {
			return pushWhenNoDuplicate(request, task);
		} else {
			return PUSH_DUPLICATE;
		}
	}
    
    abstract int pushWhenNoDuplicate(Request request, Task task);
}
