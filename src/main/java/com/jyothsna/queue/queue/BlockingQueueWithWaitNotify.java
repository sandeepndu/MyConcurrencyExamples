package com.jyothsna.queue.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueueWithWaitNotify<T> {
    private static final Logger logger = LoggerFactory.getLogger(BlockingQueueWithWaitNotify.class);
    private final int capacity;
    private final Queue<T> queue;

    public BlockingQueueWithWaitNotify(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    public BlockingQueueWithWaitNotify() {
        this.capacity = Integer.MAX_VALUE;
        this.queue = new LinkedList<>();
    }

    public void offer(T element) throws InterruptedException {
        synchronized (queue) {
            while (queue.size() == capacity) {
                logger.info("Queue is full. Waiting for a slot to insert {}. Size: {}", element, queue.size());
                queue.wait();
            }
            queue.offer(element);
            logger.info("Pushed {} to queue. Size: {}", element, queue.size());
            queue.notifyAll();
        }
    }

    public T poll() throws InterruptedException {
        synchronized (queue) {
            while (queue.isEmpty()) {
                logger.info("Waiting for something to be inserted to queue. Size: {}", queue.size());
                wait();
            }
            T element = queue.poll();
            logger.info("Removed {} from queue. Size: {}", element, queue.size());
            queue.notifyAll();
            return element;
        }
    }
}
