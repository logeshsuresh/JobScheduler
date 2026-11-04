package org.logesh.entity;

import org.logesh.enums.TaskType;
import org.logesh.service.SchedulerService;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Job implements Runnable, Comparable<Job>  {

    private final int jobId;
    private final Runnable task;
    private final Date startTime;
    private final long reschedulePeriod;
    private final TimeUnit unit;
    private final TaskType taskType;

    public Job(int jobId, Runnable task, Date startTime, long reschedulePeriod, TimeUnit unit, TaskType taskType) {
        this.jobId = jobId;
        this.task = task;
        this.startTime = startTime;
        this.reschedulePeriod = reschedulePeriod;
        this.unit = unit;
        this.taskType = taskType;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    @Override
    public int compareTo(Job o) {
        return this.startTime.compareTo(o.startTime);
    }

    @Override
    public void run() {
        if (TaskType.REPEAT_FIXED_RATE.equals(this.taskType)) {
            // Recursively schedule the next task even before completing the current one
            SchedulerService.getInstance().scheduleAtFixedRate(this.task, this.reschedulePeriod, this.reschedulePeriod, unit);
        }

        System.out.printf("Running main.entity.Job : %s at time %d%n", this.jobId, Calendar.getInstance().get(Calendar.SECOND));
        this.task.run(); // sync - wait.

    }
}
