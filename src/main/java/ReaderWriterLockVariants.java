import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReaderWriterLockVariants {
    public static void main(String[] args) throws InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final WriterFavouredLock lock = new WriterFavouredLock();
        final Random rn = new Random();
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        lock.lockRead();
                        final int waitTime = rn.nextInt(5);
                        System.out.println("reading ..for " + waitTime + " seconds");
                        Thread.sleep(waitTime * 1000);
                        lock.unlockRead();
//                        Thread.sleep(waitTime * 2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            });
        }


        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        lock.lockWrite();
                        final int waitTime = rn.nextInt(5);
                        System.out.println("writing .. for " + waitTime + " seconds");
                        Thread.sleep(waitTime * 1000);
                        lock.unlockWrite();
//                        Thread.sleep(waitTime * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            });
        }

        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    public static class FairLock{
        final Lock lock = new ReentrantLock();
        final Condition waitingWriter = lock.newCondition();
        final Condition waitingReader = lock.newCondition();
        final Condition reader = lock.newCondition();
        final Condition writer = lock.newCondition();
        int readers = 0;
        int writers = 0;
        int waitingWriters = 0;
        int waitingReaders =0;

        public void lockRead() throws InterruptedException {
            lock.lock();
            try {
                while (writers>0 || waitingWriters>0) {

                }
            }finally {
                lock.unlock();
            }
        }

        public void unlockRead() {
            lock.lock();
            try {

            }finally {
                lock.unlock();
            }
        }

        public void lockWrite() {
            lock.lock();
            try {

            }finally {
                lock.unlock();
            }
        }

        public void unlockWrite() {
            lock.lock();
            try {

            }finally {
                lock.unlock();
            }
        }
    }

    public static class WriterFavouredLock {
        final Lock lock = new ReentrantLock();
        final Condition waitingWriter = lock.newCondition();
        final Condition reader = lock.newCondition();
        final Condition writer = lock.newCondition();
        int readers = 0;
        int writers = 0;
        int waitingWriters = 0;


        public void lockRead() throws InterruptedException {
            lock.lock();
            try {
                while (writers > 0 || waitingWriters > 0) {
                    if(writers>0) {
                        writer.await();
                    } else {
                        waitingWriter.await();
                    }
                }
                readers++;
            } finally {
                lock.unlock();
            }
        }

        public void unlockRead() {
            lock.lock();
            try {
                readers--;
                reader.signalAll();
                writer.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void lockWrite() throws InterruptedException {
            lock.lock();
            try {
                boolean hasToBeInWaitingQueue = false;
                if (readers > 0 || writers > 0) {
                    waitingWriters++;
                    hasToBeInWaitingQueue = true;
                }
                if (readers > 0) {
                    reader.await();
                }
                if (writers > 0) {
                    writer.await();
                }
                writers++;
                if (hasToBeInWaitingQueue) {
                    waitingWriters--;
                }
            } finally {
                lock.unlock();
            }

        }

        public void unlockWrite() {
            lock.lock();
            try {
                writers--;
                reader.signalAll();
                writer.signalAll();
                waitingWriter.signalAll();
            } finally {
                lock.unlock();
            }

        }
    }

    // Reader writer lock with no fairness, this might lead to starvation
    // if many readers writers will be starved
    public static class ReaderFavouredLock {

        final Lock lock = new ReentrantLock();
        final Condition writer = lock.newCondition();
        final Condition reader = lock.newCondition();

        int numberOfReaders = 0;
        int numberOfWriters = 0;

        void lockRead() throws InterruptedException {
            lock.lock();
            try {
                while (numberOfWriters > 0) {
                    writer.await();
                }
                numberOfReaders++;
            } finally {
                lock.unlock();
            }
        }

        void unlockRead() {
            lock.lock();
            try {
                numberOfReaders--;
                reader.signal();
                writer.signal();
            } finally {
                lock.unlock();
            }
        }

        void lockWrite() throws InterruptedException {
            lock.lock();
            try {
                while (numberOfReaders > 0) {
                    reader.await();
                }
                while (numberOfWriters > 0) {
                    writer.await();
                }
                numberOfWriters++;
            } finally {
                lock.unlock();
            }

        }

        void unlockWrite() {
            lock.lock();
            try {
                numberOfWriters--;
                writer.signal();
                reader.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    public static class ReaderWriterLockWaitNotify {
        int readers = 0, writers = 0;

        synchronized void lockRead() throws InterruptedException {
            while (writers > 0) {
                wait();
            }
            readers++;
        }

        synchronized void unlockRead() {
            readers--;
            notifyAll();
        }

        synchronized void lockWrite() throws InterruptedException {
            while (readers > 0 || writers > 0) {
                wait();
            }
            writers++;
        }

        synchronized void unlockWrite() {
            writers--;
            notifyAll();
        }
    }
}
