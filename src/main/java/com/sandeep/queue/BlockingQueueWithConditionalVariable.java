package com.sandeep.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueWithConditionalVariable<T> implements BlockingQueue<T> {
    private static final Logger logger = LoggerFactory.getLogger(BlockingQueueWithConditionalVariable.class);
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<T> queue = new LinkedList<>();

    public BlockingQueueWithConditionalVariable(int capacity) {
        this.capacity = capacity;
    }

    public BlockingQueueWithConditionalVariable() {
        this.capacity = Integer.MAX_VALUE;
    }

    @Override
    public void offer(T element) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                logger.info("Queue is full. Waiting for a slot to insert {}. Size: {}", element, queue.size());
                notFull.await();
            }
            queue.offer(element);
            logger.info("Pushed {} to queue. Size: {}", element, queue.size());
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T poll() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                logger.info("Waiting for something to be inserted to queue. Size: 0");
                notEmpty.await();
            }
            T element = queue.poll();
            logger.info("Removed {} from queue. Size: {}", element, queue.size());
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }
}
