package com.sandeep.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class BlockingQueueWithSemaphores<T> implements BlockingQueue<T> {
    private static final Logger logger = LoggerFactory.getLogger(BlockingQueueWithSemaphores.class);
    private final int capacity;
    private final Queue<T> queue;
    private final Semaphore writeSemaphore;
    private final Semaphore pollSemaphore;

    public BlockingQueueWithSemaphores(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
        this.writeSemaphore = new Semaphore(capacity);
        this.pollSemaphore = new Semaphore(0);
    }

    public BlockingQueueWithSemaphores() {
        this.capacity = Integer.MAX_VALUE;
        this.queue = new LinkedList<>();
        this.writeSemaphore = new Semaphore(capacity);
        this.pollSemaphore = new Semaphore(0);
    }

    @Override
    public void offer(T element) throws InterruptedException {
        writeSemaphore.acquire();
        synchronized (this) {
            queue.offer(element);
            logger.info("Pushed {} to queue. Size: {}", element, queue.size());
        }
        pollSemaphore.release();
    }

    @Override
    public T poll() throws InterruptedException {
        pollSemaphore.acquire();
        final T element;
        synchronized (this) {
            element = queue.poll();
            logger.info("Removed {} from queue. Size: {}", element, queue.size());
        }
        writeSemaphore.release();
        return element;
    }
}
