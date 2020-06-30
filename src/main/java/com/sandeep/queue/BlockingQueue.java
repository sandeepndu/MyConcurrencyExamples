package com.sandeep.queue;

public interface BlockingQueue<T> {
    void offer(T element) throws InterruptedException;

    T poll() throws InterruptedException;
}
