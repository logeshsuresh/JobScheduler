package org.logesh.service;

import java.util.concurrent.TimeUnit;

public interface ISchedulerService {
    void schedule(Runnable task, long initialDelay, TimeUnit unit);
    void scheduleAtFixedRate(Runnable task, long delay, long recurringDelay, TimeUnit unit);
}
