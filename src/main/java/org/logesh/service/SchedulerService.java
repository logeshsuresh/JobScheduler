package org.logesh.service;

import org.logesh.entity.Job;
import org.logesh.enums.TaskType;
import org.logesh.executor.JobExecutor;

import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SchedulerService implements ISchedulerService {

    private static final SchedulerService INSTANCE = new SchedulerService(Runtime.getRuntime().availableProcessors());
    private final PriorityQueue<Job> jobPriorityQueue;
    private final Lock queueLock;
    private final Condition entryAdded;

    private SchedulerService(int threadCount) {
        this.jobPriorityQueue = new PriorityQueue<>();
        this.queueLock = new ReentrantLock();
        this.entryAdded = queueLock.newCondition();

        // start a separate thread for JobExecutor
        Thread executor = new Thread(new JobExecutor(this.jobPriorityQueue, this.queueLock, this.entryAdded, threadCount));
        executor.start();
    }

    public static SchedulerService getInstance() {
        return INSTANCE;
    }

    @Override
    public void schedule(Runnable task, long initialDealy, TimeUnit unit) {
        Date date = new Date(Calendar.getInstance().getTime().getTime() + unit.toMillis(initialDealy));
        Job job = new Job(UUID.randomUUID().hashCode(), task, date, 0L, null, TaskType.ONCE);
        addToJobQueue(job);
    }

    @Override
    public void scheduleAtFixedRate(Runnable task, long initialDealy, long recurringDelay, TimeUnit unit) {
        Date date = new Date(Calendar.getInstance().getTime().getTime() + unit.toMillis(initialDealy));
        Job job = new Job(UUID.randomUUID().hashCode(), task, date, recurringDelay, unit, TaskType.REPEAT_FIXED_RATE);
        addToJobQueue(job);
    }

    private void addToJobQueue(Job job) {
        this.queueLock.lock();
        try {
            this.jobPriorityQueue.offer(job);
            entryAdded.signal();
        } finally {
            this.queueLock.unlock();
        }
    }


}
