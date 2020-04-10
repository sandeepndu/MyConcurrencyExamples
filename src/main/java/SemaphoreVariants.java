import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreVariants {
    public static class SemaphoreCv {
        private Lock lock = new ReentrantLock();
        private Condition permitAvailability = lock.newCondition();
        private int permits;

        public SemaphoreCv(int permits) {
            this.permits = permits;
        }

        public void acquire() throws InterruptedException {
            lock.lock();
            try {
                if (permits == 0) {
                    permitAvailability.await();
                }
                permits--;
            } finally {
                lock.unlock();
            }
        }

        public void release() {
            lock.lock();
            try {
                permits++;
                permitAvailability.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
