import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousQueueVariants {
    // Offer should block until there is a consumer
    public static void main(String[] args) throws InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Random rn = new Random();
        final SynchronousQueueWithConditionalVariable<Integer> queue = new SynchronousQueueWithConditionalVariable<>(3);
        for (int i = 0; i < 8; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        Thread.sleep((rn.nextInt(4) + 1) * 2000);
                        queue.offer(rn.nextInt(10));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
        }

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        Thread.sleep((rn.nextInt(4) + 1) * 1000);
                        queue.take();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }


    public static class SynchronousQueueWithConditionalVariable<T> {
        private final int capacity;
        private final Lock lock = new ReentrantLock();
        final Condition thereIsProducer = lock.newCondition();
        final Condition thereIsConsumer = lock.newCondition();
        final Condition queueNotFull = lock.newCondition();
        private final Queue<T> producerQueue;
        int consumerCount = 0;

        public SynchronousQueueWithConditionalVariable(int capacity) {
            producerQueue = new LinkedList<>();
            this.capacity = capacity;
        }

        public void offer(final T produce) throws InterruptedException {
            lock.lock();
            try {
                while (producerQueue.size() >= capacity) {
                    queueNotFull.await();
                }
                producerQueue.offer(produce);
                System.out.println("waiting for consumer to hand off " + produce);
                while (consumerCount == 0) {
                    thereIsConsumer.await();
                }
                thereIsProducer.signalAll();
            } finally {
                lock.unlock();
            }

        }

        public T take() throws InterruptedException {
            lock.lock();
            try {
                consumerCount++;
                thereIsConsumer.signalAll();

                while (producerQueue.isEmpty()) {
                    thereIsProducer.await();
                }
                final T res = producerQueue.poll();
                System.out.println("Handed off " + res + " to consumer");
                queueNotFull.signalAll();
                return res;
            } catch (Exception e) {
                System.out.println(e);
                return null;
            } finally {
                lock.unlock();
            }
        }
    }

    public static class SynchronousQueueWithWaitNotify<T> {
        private final BlockingQueue<T> queue;

        public SynchronousQueueWithWaitNotify(int capacity) {
            queue = new ArrayBlockingQueue<>(capacity);
        }

        public void offer(final T produce) throws InterruptedException {
            synchronized (produce) {
                queue.offer(produce);
                System.out.println("waiting for consumer to hand off " + produce);
                produce.wait();
            }
        }

        public T take() throws InterruptedException {
            final T top = queue.take();
            synchronized (top) {
                top.notify();
                System.out.println("Handed off " + top + " to consumer");
            }
            return top;
        }

    }
}
