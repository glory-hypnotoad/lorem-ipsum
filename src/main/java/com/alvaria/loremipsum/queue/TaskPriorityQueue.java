package com.alvaria.loremipsum.queue;

import com.alvaria.loremipsum.redblacktree.RedBlackTree;
import com.alvaria.loremipsum.tasks.IDTask;
import com.alvaria.loremipsum.tasks.RankedTask;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
    RedBlackTree<IDTask> idTaskTree;
    RedBlackTree<RankedTask> rankedTaskTree;

    public enum Status {
        S_OK,
        E_NEGATIVE_ID,
        E_INVALID_ENQUEUE_TIME,
        E_ID_ALREADY_EXISTS,
        E_RANKED_TASK_ALREADY_EXISTS
    }

    public TaskPriorityQueue() {
        idTaskTree = new RedBlackTree<>();
        rankedTaskTree = new RedBlackTree<>();
    }

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

        return Status.S_OK;
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
