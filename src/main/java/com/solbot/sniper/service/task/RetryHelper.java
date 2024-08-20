package com.solbot.sniper.service.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class RetryHelper<T> {
    private static final Logger LOG = LoggerFactory.getLogger(RetryHelper.class);
    private final ScheduledExecutorService executorService;
    private final Callable<T> task;
    private final long retryIntervalMillis;
    private final int maxRetry;


    public RetryHelper(ScheduledExecutorService executorService,
                       Callable<T> task,
                       long retryIntervalMillis,
                       int maxRetry) {
        this.executorService = executorService;
        this.task = task;
        this.retryIntervalMillis = retryIntervalMillis;
        this.maxRetry = maxRetry;
    }

    public T retry() {
        int retry = 1;
        do{
            Future<T> future = executorService.schedule(task, retry == 1 ? 0L : retryIntervalMillis, TimeUnit.MILLISECONDS);
            if (future.isDone()) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                   LOG.error("Error in retying the task", e);
                }
            }
            retry++;
        }
        while (retry <= maxRetry);
        return null;
    }
}
