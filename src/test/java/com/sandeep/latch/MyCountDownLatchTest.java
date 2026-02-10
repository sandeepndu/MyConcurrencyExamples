package com.sandeep.latch;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MyCountDownLatchTest {

    @Test
    void testAwaitReleasedAfterCountDown() throws InterruptedException {
        MyCountDownLatch latch = new MyCountDownLatch(2);
        AtomicBoolean released = new AtomicBoolean(false);

        Thread t = new Thread(() -> {
            try {
                latch.await();
                released.set(true);
            } catch (InterruptedException e) {
                // ignore
            }
        });
        t.start();
        Thread.sleep(100); // ensure t starts and waits
        assertFalse(released.get(), "Thread should not be released before countDown");

        latch.countDown();
        Thread.sleep(100);
        assertFalse(released.get(), "Thread should not be released until count reaches 0");

        latch.countDown(); // count == 0, should release
        Thread.sleep(100);
        assertTrue(released.get(), "Thread should be released after count reaches 0");
    }

    @Test
    void testMultipleThreadsReleased() throws InterruptedException {
        MyCountDownLatch latch = new MyCountDownLatch(1);
        AtomicInteger released = new AtomicInteger(0);

        Runnable awaiter = () -> {
            try {
                latch.await();
                released.incrementAndGet();
            } catch (InterruptedException e) {
                // ignore
            }
        };

        Thread t1 = new Thread(awaiter);
        Thread t2 = new Thread(awaiter);
        t1.start();
        t2.start();

        Thread.sleep(100);
        assertEquals(0, released.get(), "No thread should be released yet");
        latch.countDown();
        Thread.sleep(100);
        assertEquals(2, released.get(), "Both threads should be released after countDown");
    }

    @Test
    void testCountDownDoesNotGoBelowZero() throws InterruptedException {
        MyCountDownLatch latch = new MyCountDownLatch(1);
        latch.countDown();
        // should now be at 0
        for (int i = 0; i < 5; i++) {
            latch.countDown(); // further decrements have no effect
        }
        // Check that await returns immediately
        latch.await();
    }

    @Test
    void testConstructorNegativeCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new MyCountDownLatch(-1));
    }

    @Test
    void testAwaitReturnsImmediatelyIfCountZero() throws InterruptedException {
        MyCountDownLatch latch = new MyCountDownLatch(0);
        latch.await(); // Should return immediately, not throw or hang
    }
}