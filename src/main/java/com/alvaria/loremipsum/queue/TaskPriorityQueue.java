package com.alvaria.loremipsum.queue;

import com.alvaria.loremipsum.redblacktree.RedBlackTree;
import com.alvaria.loremipsum.tasks.IDTask;
import com.alvaria.loremipsum.tasks.RankedTask;
import lombok.extern.slf4j.Slf4j;

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
public class TaskPriorityQueue {
    RedBlackTree<IDTask> idTaskTree;
    RedBlackTree<RankedTask> rankedTaskTree;

    public void addNewTask(Long id, Long enqueueTime) {
        IDTask newIdTask = new IDTask(id);
        RankedTask newRankedTask = new RankedTask(id, enqueueTime);
        newIdTask.setLinkedRankedTask(newRankedTask);
        newRankedTask.setLinkedIdTask(newIdTask);

        idTaskTree.insertNode(newIdTask);
        rankedTaskTree.insertNode(newRankedTask);
    }
}
