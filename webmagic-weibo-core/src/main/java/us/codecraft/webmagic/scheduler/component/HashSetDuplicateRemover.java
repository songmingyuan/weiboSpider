package us.codecraft.webmagic.scheduler.component;

import com.google.common.collect.Sets;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author code4crafer@gmail.com
 */
public class HashSetDuplicateRemover implements DuplicateRemover {

    private Set<String> urls = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public boolean isDuplicate(Request request, Task task) {
        String url = getUrl(request);
		// 如果以/结尾的话，去掉/
		int lastIndex = url.length() -1;
		if (url.charAt(lastIndex) == '/') {
			url = url.substring(0, lastIndex);
		}
		return !urls.add(url);
    }

    protected String getUrl(Request request) {
        return request.getUrl();
    }

    @Override
    public void resetDuplicateCheck(Task task) {
        urls.clear();
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return urls.size();
    }
}
