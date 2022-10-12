package com.alvaria.loremipsum.tasks;

/**
 * The {@code Task} class represents a single task that can be queued.
 * Task objects can be compared using their class (depends on the ID) and age.
 * This class is used for red-black tree that is sorted based on the Task rank
 * and allows to search for a Task with the highest rank with logarithmic
 * complexity
 *
 * @author Nikita Nikolaev
 */
public class Task extends BaseTask {

    private enum TaskClass {
        NORMAL,
        PRIORITY,
        VIP,
        MANAGEMENT_OVERRIDE
    }

    private Long enqueueTime;
    private TaskClass taskClass;

    public Task(Long id, Long enqueueTime) {
        super(id);
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
     * Returns the actual Task's rank depending on the taskClass
     * and age. Note that this is not enough to compare the Task
     * objects as TaskClass.MANAGEMENT_OVERRIDE must always have
     * higher priority.
     *
     * @return current rank depending on the task class and age
     */
    private double getCurrentRank() {
        Long currentTime = System.currentTimeMillis() / 1000L;
        Long secondsInQueue = currentTime - enqueueTime;

        switch (taskClass) {
            case VIP:
                return Math.max(4.0, 2 * secondsInQueue * Math.log(secondsInQueue));

            case PRIORITY:
                return  Math.max(3.0, secondsInQueue * Math.log(secondsInQueue));

            default:
                return (double)secondsInQueue;
        }
    }

    /**
     * Compares two Task objects
     * @param otherBaseTask the object to be compared.
     * @return {@code 1} if this object is greater than the specified object;
     *         {@code -1} if this object is less than the specified object;
     *         {@code 0} if the objects are equal.
     */
    @Override
    public int compareTo(BaseTask otherBaseTask) {
        Task otherTask;
        if (!(otherBaseTask instanceof Task)) {
            throw new IllegalArgumentException("Cannot compare BaseTask and Task objects");
        } else {
            otherTask = (Task)otherBaseTask;
        }

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
            if (this.id > otherTask.id) {
            return 1;
        } else if (this.id < otherTask.id) {
            return -1;
        } else {
            return 0;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getEnqueueTime() {
        return enqueueTime;
    }

}
