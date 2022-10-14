package com.alvaria.loremipsum.queue;

import com.alvaria.loremipsum.redblacktree.Node;
import com.alvaria.loremipsum.redblacktree.RedBlackTree;
import com.alvaria.loremipsum.tasks.IDTask;
import com.alvaria.loremipsum.tasks.RankedTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * The TaskPriorityQueue class represents the priority queue
 * for tasks that allows to get the highest (lowest) priority task or perform
 * operations with a task of given ID.
 *
 * The class is organised based on two Red-Black trees: one of them is sorted
 * based on the IDs and another one is sorted based on the Task ranks
 *
 * @author Nikita Nikolaev
 */
@Slf4j
@Component
public class TaskPriorityQueue {
    final RedBlackTree<IDTask> idTaskTree;
    final RedBlackTree<RankedTask> rankedTaskTree;

    /**
     * Possible operation statuses
     */
    public enum Status {
        S_OK,
        E_NEGATIVE_ID,
        E_INVALID_ENQUEUE_TIME,
        E_ID_ALREADY_EXISTS,
        E_RANKED_TASK_ALREADY_EXISTS
    }

    /**
     * Default constructor
     */
    public TaskPriorityQueue() {
        idTaskTree = new RedBlackTree<>();
        rankedTaskTree = new RedBlackTree<>();
    }

    /**
     * Add new task to the queue
     * @param id task ID
     * @param enqueueTime UTC time when the task was enqueued (must be in past)
     * @return Status of operation
     */
    public Status addNewTask(Long id, Long enqueueTime) {
        String methodName = "addNewTask";

        log.info("{}: Trying to add a new Task: id = {}, enqueueTime = {}", methodName, id, enqueueTime);

        Status status = validateId(id);
        if (status != Status.S_OK) {
            log.info("{}: negative ID cannot be accepted", methodName);
            return status;
        }

        status = validateEnqueueTime(enqueueTime);
        if (status != Status.S_OK) {
            log.info("{}: enqueue time is invalid: {}; current UTC epoch is {}", methodName, enqueueTime, Instant.now().getEpochSecond());
            return status;
        }

        IDTask newIdTask = new IDTask(id);
        RankedTask newRankedTask = new RankedTask(id, enqueueTime);
        newIdTask.setLinkedRankedTask(newRankedTask);
        newRankedTask.setLinkedIdTask(newIdTask);

        synchronized (idTaskTree) {
            synchronized (rankedTaskTree) {
                try {
                    log.info("{}: inserting new node to the ID tree", methodName);
                    idTaskTree.insertNode(newIdTask);
                    log.info("{}: node inserted successfully", methodName);
                } catch (IllegalArgumentException ex) {
                    log.warn("{}: The task with the specified ID already exists", methodName);
                    return Status.E_ID_ALREADY_EXISTS;
                }

                try {
                    log.info("{}: inserting new node to the ranked task tree", methodName);
                    rankedTaskTree.insertNode(newRankedTask);
                    log.info("{}: node inserted successfully", methodName);
                } catch (IllegalArgumentException ex) {
                    // Report this as an error because the trees are out of sync if we didn't get
                    // this exception on the previous step
                    log.error("{}: and equal ranked task already exists", methodName);
                    return Status.E_RANKED_TASK_ALREADY_EXISTS;
                }
            }
        }

        return Status.S_OK;
    }

    public RankedTask poll() {
        String methodName = "poll";
        synchronized (idTaskTree) {
            synchronized (rankedTaskTree) {
                log.info("{}: Polling the ranked tree", methodName);
                RankedTask task = rankedTaskTree.pollMaximum();
                if (task != null) {
                    log.info("{}: Task found - deleting the linked task from ID tree", methodName);
                    idTaskTree.deleteNode(task.getLinkedIdTask());
                    return task;
                } else {
                    log.info("{}: The tree is empty", methodName);
                    return null;
                }
            }
        }
    }

    /**
     * Get the list of all tasks in the queue sorted from the highest rank to lowest
     *
     * TODO: May be optimized (e.g. build it in the correct order straight from the tree)
     * @return Sorted list of tasks
     */
    public List<RankedTask> getRankedTaskList() {
        String methodName = "getRankedTaskList";
        synchronized (rankedTaskTree) {
            log.info("{}: building the tasks list from highest rank to lowest", methodName);
            // The list built from red-black tree is sorted from lowest to highest
            // rank - need to reverse it
            List<RankedTask> taskList = rankedTaskTree.buildNodeList();
            Collections.reverse(taskList);
            return taskList;
        }
    }

    /**
     * Get the Task's position in the ranked queue
     * @param id the task ID
     * @return the task's position
     */
    public int getTaskPosition(Long id) {
        String methodName = "getTaskPosition";
        IDTask idTask = new IDTask(id);
        synchronized (idTaskTree) {
            Node<IDTask> idNode = idTaskTree.findValue(idTask);

            log.info("{}}: Trying to get the position of task: {}", methodName, id);

            if (idNode == null) {
                // The task is not queued
                log.info("{}}: Task {} is not queued", methodName, id);
                return -1;
            } else {
                log.info("{}}: Task {} exists - getting the position", methodName, id);
                List<RankedTask> taskList = getRankedTaskList();
                RankedTask rankedTask = idNode.getData().getLinkedRankedTask();
                return taskList.indexOf(rankedTask);
            }
        }
    }

    private Status validateId(Long id) {
        if (id <= 0) {
            return Status.E_NEGATIVE_ID;
        } else {
            return Status.S_OK;
        }
    }

    private Status validateEnqueueTime(Long enqueueTime) {
        long currentTime = Instant.now().getEpochSecond();
        if (enqueueTime <= 0 || enqueueTime > currentTime) {
            return Status.E_INVALID_ENQUEUE_TIME;
        } else {
            return Status.S_OK;
        }
    }

}
