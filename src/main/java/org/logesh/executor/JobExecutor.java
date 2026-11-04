package org.logesh.executor;

import org.logesh.entity.Job;

import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class JobExecutor implements Runnable {

    private final Executor jobExecutors;
    private PriorityQueue<Job> jobPriorityQueue;
    private Lock queueLock;
    private Condition entryAdded;

    public JobExecutor(PriorityQueue<Job> jobPriorityQueue, Lock queueLock, Condition entryAdded, int threadCount) {
        this.jobPriorityQueue = jobPriorityQueue;
        this.queueLock = queueLock;
        this.entryAdded = entryAdded;
        this.jobExecutors = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public void run() {
        while (true) {
            this.queueLock.lock();
            try {
                if (!this.jobPriorityQueue.isEmpty()) {
                    Job job = this.jobPriorityQueue.peek();
                    Date startTime = job.getStartTime();
                    Date current = Calendar.getInstance().getTime();
                    if (current.compareTo(startTime) >= 0) {
                        this.jobPriorityQueue.remove();
                        this.jobExecutors.execute(job);
                    }
                } else {
                    try {
                        this.entryAdded.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                this.queueLock.unlock();
            }
        }
    }
}
