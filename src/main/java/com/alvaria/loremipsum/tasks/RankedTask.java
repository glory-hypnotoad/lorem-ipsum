package com.alvaria.loremipsum.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.time.Instant;

/**
 * The {@code RankedTask} class represents a single task that can be queued.
 * Task objects can be compared using their class (depends on the ID) and age.
 * This class is used for red-black tree that is sorted based on the Task rank
 * and allows to search for a Task with the highest rank with logarithmic
 * complexity
 *
 * @author Nikita Nikolaev
 */
@Slf4j
public class RankedTask implements Comparable<RankedTask>{

    public enum TaskClass {
        NORMAL,
        PRIORITY,
        VIP,
        MANAGEMENT_OVERRIDE
    }

    private final Long id;
    private final Long enqueueTime;

    @JsonIgnore
    private final TaskClass taskClass;

    @JsonIgnore
    private IDTask linkedIdTask;

    public RankedTask(Long id, Long enqueueTime) {
        this.id = id;
        this.enqueueTime = enqueueTime;

        if ((id % 3 == 0) && (id % 5 == 0)) {
            this.taskClass = TaskClass.MANAGEMENT_OVERRIDE;
        } else if (id % 5 == 0) {
            this.taskClass = TaskClass.VIP;
        } else if (id % 3 == 0) {
            this.taskClass = TaskClass.PRIORITY;
        } else {
            this.taskClass = TaskClass.NORMAL;
        }
    }

    /**
     * Returns the actual RankedTask's rank depending on the taskClass
     * and age. Note that this is not enough to compare the Task
     * objects as TaskClass.MANAGEMENT_OVERRIDE must always have
     * higher priority.
     *
     * @return current rank depending on the task class and age
     */
    public double getCurrentRank() {
        Long currentTime = Instant.now().getEpochSecond();
        long secondsInQueue = currentTime - enqueueTime;

        return switch (taskClass) {
            case VIP -> Math.max(4.0, 2 * secondsInQueue * Math.log(secondsInQueue));
            case PRIORITY -> Math.max(3.0, secondsInQueue * Math.log(secondsInQueue));
            default -> (double) secondsInQueue;
        };
    }

    /**
     * Compares two RankedTask objects
     * @param otherTask the object to be compared.
     * @return {@code 1} if this object is greater than the specified object;
     *         {@code -1} if this object is less than the specified object;
     *         {@code 0} if the objects are equal.
     */
    @Override
    public int compareTo(RankedTask otherTask) {
        // The older task must be ranked higher
        return otherTask.enqueueTime.compareTo(this.enqueueTime);
    }

    public Long getId() {
        return id;
    }

    public Long getEnqueueTime() {
        return enqueueTime;
    }

    public void setLinkedIdTask(IDTask linkedIdTask) {
        this.linkedIdTask = linkedIdTask;
    }

    public IDTask getLinkedIdTask() {
        return linkedIdTask;
    }

    public TaskClass getTaskClass() {
        return taskClass;
    }
}
