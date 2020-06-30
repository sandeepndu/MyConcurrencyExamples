package com.sandeep.queue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class BlockingQueueWithWaitNotifyTest {
    private static final Logger logger = LoggerFactory.getLogger(BlockingQueueWithWaitNotifyTest.class);
    private static Random random;
    private static ExecutorService executorService;
    private static final int THREAD_COUNT = 10;

    @BeforeAll
    private static void beforeAll() {
        random = new Random();
    }

    @BeforeEach
    private void beforeEach() {
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    private void afterEach() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    public void fixedSizeBlockingQueueTest() {
        BlockingQueueWithWaitNotify<Integer> blockingQueue = new BlockingQueueWithWaitNotify<>(3);
        IntStream.rangeClosed(1, THREAD_COUNT / 2)
                .forEach(index -> executorService.submit(() -> {
                    try {
                        Thread.sleep(500);
                        blockingQueue.offer(random.nextInt(100));
                    } catch (InterruptedException ex) {
                        logger.error("Insert failed", ex);
                    }
                }));
        IntStream.rangeClosed(1, THREAD_COUNT / 2)
                .forEach(index -> executorService.submit(() -> {
                    try {
                        Thread.sleep(1000);
                        blockingQueue.poll();
                    } catch (InterruptedException ex) {
                        logger.error("Remove failed", ex);
                    }
                }));
    }

    @Test
    public void defaultSizeBlockingQueueTest() {
        BlockingQueueWithWaitNotify<Integer> blockingQueue = new BlockingQueueWithWaitNotify<>();
        IntStream.rangeClosed(1, THREAD_COUNT / 2)
                .forEach(index -> executorService.submit(() -> {
                    try {
                        Thread.sleep(500);
                        blockingQueue.offer(random.nextInt(100));
                    } catch (InterruptedException ex) {
                        logger.error("Insert failed", ex);
                    }
                }));
        IntStream.rangeClosed(1, THREAD_COUNT / 2)
                .forEach(index -> executorService.submit(() -> {
                    try {
                        Thread.sleep(1000);
                        blockingQueue.poll();
                    } catch (InterruptedException ex) {
                        logger.error("Remove failed", ex);
                    }
                }));
    }
}
