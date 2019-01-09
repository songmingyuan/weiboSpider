package com.cmcc.wltx.collector.spider.mywebmagic.scheduler;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * app爬虫在用。单队列
 * @author Future
 *
 */
public class FileQueueScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, Closeable {
	
	private ScheduledExecutorService stp;

    private String filePath = System.getProperty("java.io.tmpdir");

    private String fileUrlAllName = ".urls.txt";

    private Task task;

    private PrintWriter fileUrlWriter;

    private AtomicBoolean inited = new AtomicBoolean(false);

    private BlockingQueue<Request> queue;

    private Set<String> urls;

    public FileQueueScheduler(String filePath) {
        if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
            filePath += "/";
        }
        this.filePath = filePath;
        initDuplicateRemover();
    }

    private void flush() {
        fileUrlWriter.flush();
    }

    private void init(Task task) {
        this.task = task;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        readFile();
        initWriter();
        initFlushThread();
        inited.set(true);
        logger.info("init cache scheduler success");
    }

    private void initDuplicateRemover() {
        setDuplicateRemover(
                new DuplicateRemover() {
                    @Override
                    public boolean isDuplicate(Request request, Task task) {
                        if (!inited.get()) {
                            init(task);
                        }
                        return !urls.add(request.getUrl());
                    }

                    @Override
                    public void resetDuplicateCheck(Task task) {
                        urls.clear();
                    }

                    @Override
                    public int getTotalRequestsCount(Task task) {
                        return urls.size();
                    }
                });
    }

    private void initFlushThread() {
        stp = Executors.newScheduledThreadPool(1);
        stp.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void initWriter() {
        try {
            fileUrlWriter = new PrintWriter(new FileWriter(getFileName(fileUrlAllName), true));
        } catch (IOException e) {
            throw new RuntimeException("init cache scheduler error", e);
        }
    }

    private void readFile() {
        try {
            queue = new LinkedBlockingQueue<Request>();
            urls = new LinkedHashSet<String>();
            readUrlFile();
        } catch (FileNotFoundException e) {
            //init
            logger.info("init cache file " + getFileName(fileUrlAllName));
        } catch (IOException e) {
            logger.error("init file error", e);
        }
    }

    private void readUrlFile() throws IOException {
        String line;
        BufferedReader fileUrlReader = null;
        try {
            fileUrlReader = new BufferedReader(new FileReader(getFileName(fileUrlAllName)));
            while ((line = fileUrlReader.readLine()) != null) {
                urls.add(line.trim());
            }
        } finally {
            if (fileUrlReader != null) {
                IOUtils.closeQuietly(fileUrlReader);
            }
        }
    }

    private String getFileName(String filename) {
        return filePath + task.getUUID() + filename;
    }

    @Override
    protected boolean pushWhenNoDuplicate(Request request, Task task) {
    	return queue.add(request);
    }

    @Override
    public synchronized Request poll(Task task) {
        if (!inited.get()) {
            init(task);
        }
        Request request = queue.poll();
        if (null != request && !request.isReusable()) {
        	fileUrlWriter.println(request.getUrl());
		}
		return request;
    }

    @Override
    public int getLeftRequestsCount(Task task) {
        return queue.size();
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return getDuplicateRemover().getTotalRequestsCount(task);
    }
    
    public void reloadFile(){
    	readFile();
    }
    
    public void close(){
    	flush();
    	stp.shutdown();
    }
}
