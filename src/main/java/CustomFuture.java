import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class CustomFuture<T> {
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        final CompletableFuture<Object> f = new CompletableFuture<>();
//        f.get();
//    }
//    private final Lock lock = new ReentrantLock();
//    private final Condition condition = lock.newCondition();
//    private boolean isFinished = false;
//    CustomFuture(C) {
//
//    }
//
//    public T get() {
//
//    }
//}

public class CustomFuture {
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition isFull = lock.newCondition();
    private final Condition isEmpty = lock.newCondition();
    private List<Thread> threads = new ArrayList<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private volatile boolean shutdown = false;

    CustomFuture(int capacity) {
        this.capacity = capacity;
        for (int i = 0; i < capacity; i++) {
            threads.add(new Thread(() -> {
                while (!shutdown || !queue.isEmpty()) {
                    try {
                        Runnable runnable;
                        lock.lock();
                        try {
                            while (queue.isEmpty()) {
                                isEmpty.await();
                            }
                            runnable = queue.poll();
                            isFull.signal();
                        } finally {
                            lock.unlock();
                        }
                        runnable.run();
                    } catch (InterruptedException e) {
                        System.out.println("interrupt called");
                        if (shutdown && queue.isEmpty()) {
                            break;
                        }
                    }
                }
            }));
            threads.get(i).start();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final CustomFuture executorService = new CustomFuture(3);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executorService.submit(() -> {
                System.out.printf("task %d\n", finalI);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            });
        }
        executorService.shutDown();
    }

    public void submit(final Runnable task) {
        if (shutdown) {
            return;
        }
        queue.add(task);
        lock.lock();
        isEmpty.signal();
        lock.unlock();

    }

    public void shutDown() {
        shutdown = true;
        for (final Thread thread : threads) thread.interrupt();
    }
}
