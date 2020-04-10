import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountDownLatchVariants {
    private int count;
    private Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public CountDownLatchVariants(int count) {
        this.count = count;
    }

    public void countDown() {
        lock.lock();
        try {
            count--;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void await() throws InterruptedException {
        lock.lock();
        try {
            while (count > 0) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
