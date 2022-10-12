package com.alvaria.loremipsum.queue;

import com.alvaria.loremipsum.redblacktree.RedBlackTree;
import com.alvaria.loremipsum.tasks.IDTask;
import com.alvaria.loremipsum.tasks.RankedTask;

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
public class TaskPriorityQueue {
    RedBlackTree<IDTask> idTree;
    RedBlackTree<RankedTask> taskTree;
}
