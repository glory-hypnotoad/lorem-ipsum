package com.alvaria.loremipsum.tasks;

/**
 * The {@code IDTask} is a class for comparing the Task objects using their ID.
 * This class is used for red-black tree that is sorted based on the Task ID and
 * allows to search for a given ID in the queue with logarithmic complexity
 *
 * @author Nikita Nikolaev
 */
public class IDTask implements Comparable<IDTask> {

    private Long id;
    private RankedTask linkedRankedTask; // used to find the corresponding task object if another red-black tree

    public IDTask(Long id) {
        if (id <= 0) throw new IllegalArgumentException("Task ID must be positive");

        this.id = id;
        this.linkedRankedTask = null;
    }

    /**
     * Compares two Task objects
     * @param otherBaseTask the object to be compared.
     * @return {@code 1} if this object is greater than the specified object;
     *         {@code -1} if this object is less than the specified object;
     *         {@code 0} if the objects are equal.
     */
    @Override
    public int compareTo(IDTask otherBaseTask) {
        if (this.id > otherBaseTask.id) {
            return 1;
        } else if (this.id < otherBaseTask.id) {
            return -1;
        } else {
            return 0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setLinkedRankedTask(RankedTask linkedRankedTask) {
        this.linkedRankedTask = linkedRankedTask;
    }
}
