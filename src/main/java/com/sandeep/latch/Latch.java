package com.sandeep.latch;

public interface Latch {
    void countDown();

    void await() throws InterruptedException;
}
