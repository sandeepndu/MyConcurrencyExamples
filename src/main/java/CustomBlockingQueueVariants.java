import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomBlockingQueueVariants {
    public static void main(String[] args) throws InterruptedException {
//        final BlockingQueueWithConditionalVariables<Integer> queue = new BlockingQueueWithConditionalVariables<>(3);
        final BlockingQueueWithSemaphores<Integer> queue = new BlockingQueueWithSemaphores<>(3);
//        final BlockingQueueWithWaitNotify<Integer> queue = new BlockingQueueWithWaitNotify<>(3);
        final ExecutorService es = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 6; i++) {
            es.submit(() -> {
                while (true) {
                    try {
                        Thread.sleep(500);
                        queue.offer(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("encountered exception producer");
                    }
                }

            });
        }

        for (int i = 0; i < 4; i++) {
            es.submit(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("encountered exception consumer");
                    }

                }

            });
        }
        es.shutdown();
        es.awaitTermination(5, TimeUnit.MINUTES);
    }

    public static class BlockingQueueWithSemaphores<T> {
        private final int capacity;
        private final Semaphore takeSemaphore;
        private final Semaphore putSemaphore;
        private final List<T> queue;

        public BlockingQueueWithSemaphores(int capacity) {
            this.capacity = capacity;
            this.takeSemaphore = new Semaphore(0);
            this.putSemaphore = new Semaphore(capacity);
            this.queue = new ArrayList<>(capacity);
        }

        public T take() throws InterruptedException {
            takeSemaphore.acquire();
            final T res;
            synchronized (this) {
                res = queue.remove(0);
                System.out.println(queue);
            }
            putSemaphore.release();
            return res;
        }

        public void offer(final T produce) throws InterruptedException {
            putSemaphore.acquire();
            synchronized (this){
                queue.add(produce);
                System.out.println(queue);
            }
            takeSemaphore.release();
        }
    }

    public static class BlockingQueueWithWaitNotify<T> {
        private final int size;
        private final List<T> queue;

        public BlockingQueueWithWaitNotify(final int size) {
            this.size = size;
            queue = new ArrayList<>(size);
        }

        public void offer(final T produce) throws InterruptedException {
            synchronized (queue) {
                while (queue.size() == size) {
                    queue.wait();
                }
                queue.add(produce);
                System.out.println(queue);
                queue.notifyAll();
            }

        }

        public T take() throws InterruptedException {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    queue.wait();
                }
                final T retVal = queue.remove(0);
                System.out.println(queue);
                queue.notifyAll();
                return retVal;
            }
        }
    }

    public static class BlockingQueueWithOneConditionalVariables<T> {
        private final int size;
        private final List<T> queue;
        private final Lock lock = new ReentrantLock();
        private final Condition cv = lock.newCondition();

        public BlockingQueueWithOneConditionalVariables(int size) {
            this.size = size;
            this.queue = new ArrayList<>(size);
        }

        public void offer(final T produce) {
            lock.lock();
            try {
                while (queue.size() == size) {
                    cv.await();
                }
                queue.add(produce);
                System.out.println(queue);
                cv.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public T take() {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    cv.await();
                }
                final T retVal = queue.remove(0);
                System.out.println(queue);
                cv.signalAll();
                return retVal;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } finally {
                lock.unlock();
            }
        }
    }

    public static class BlockingQueueWithConditionalVariables<T> {
        private final int size;
        private final List<T> queue;
        private final Lock lock = new ReentrantLock();
        private final Condition producerCv = lock.newCondition();
        private final Condition consumerCV = lock.newCondition();

        public BlockingQueueWithConditionalVariables(int size) {
            this.size = size;
            this.queue = new ArrayList<>(size);
        }

        public void offer(final T produce) {
            lock.lock();
            try {
                while (queue.size() == size) {
                    producerCv.await();
                }
                queue.add(produce);
                System.out.println(queue);
                consumerCV.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public T take() {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    consumerCV.await();
                }
                final T retVal = queue.remove(0);
                System.out.println(queue);
                producerCv.signalAll();
                return retVal;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } finally {
                lock.unlock();
            }
        }
    }
}
