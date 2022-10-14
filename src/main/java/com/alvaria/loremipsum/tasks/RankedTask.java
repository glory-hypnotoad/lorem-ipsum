package com.alvaria.loremipsum.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class RankedTask implements Comparable<RankedTask>{

    private enum TaskClass {
        NORMAL,
        PRIORITY,
        VIP,
        MANAGEMENT_OVERRIDE
    }

    private Long id;
    private Long enqueueTime;
    private TaskClass taskClass;

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
    private double getCurrentRank() {
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

        if ((this.taskClass == TaskClass.MANAGEMENT_OVERRIDE) && (otherTask.taskClass != TaskClass.MANAGEMENT_OVERRIDE)) {
            return 1;
        } else if ((this.taskClass != TaskClass.MANAGEMENT_OVERRIDE) && (otherTask.taskClass == TaskClass.MANAGEMENT_OVERRIDE)) {
            return -1;
        } else if (this.getCurrentRank() > otherTask.getCurrentRank()) {
            return 1;
        } else if (this.getCurrentRank() < otherTask.getCurrentRank()) {
            return -1;
        } else
            // If the ranks are equal we still need to be able to insert the task into RB-tree.
            // Therefore, we define the following logic so that two tasks with the same rank but
            // with different IDs could be compared:
            return this.id.compareTo(otherTask.id);
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

}
