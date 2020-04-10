import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PeriodicSyncingList {
    private final List<String> list1 = new ArrayList<>();
    private final List<String> list2 = new ArrayList<>();
    private boolean isList1Dirty = false;
    private boolean isList2Dirty = false;
    private ReadWriteLock lock1 = new ReentrantReadWriteLock();
    private ReadWriteLock lock2 = new ReentrantReadWriteLock();


    public static void main(String[] args) {
        final PeriodicSyncingList list = new PeriodicSyncingList();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(list::writeToDisk, 10, TimeUnit.MINUTES);

    }

    public void insertIntoList1(String a) {
        try {
            lock1.writeLock().lock();
            list1.add(a);
            isList1Dirty = true;
        } finally {
            lock1.writeLock().unlock();
        }

    }

    public List<String> getList1() {
        try {
            lock1.readLock().lock();
            return list1;
        } finally {
            lock1.readLock().unlock();
        }
    }

    public void insertIntoList2(String b) {
        try {
            lock2.writeLock().lock();
            list2.add(b);
            isList2Dirty = true;
        } finally {
            lock2.writeLock().unlock();
        }
    }

    public List<String> getList2() {
        try {
            lock2.readLock().lock();
            return list2;
        } finally {
            lock2.readLock().unlock();
        }
    }

    // This function will get called periodically by another thread.
    public void writeToDisk() {
        try {
            lock1.writeLock();
            if (isList1Dirty) {
                //db.writeAsync(listA);
                isList1Dirty = false;
            }
        } finally {
            lock1.writeLock().unlock();
        }

        try {
            lock2.writeLock().lock();
            if (isList2Dirty) {
                //db.writeAsync(listB)
                isList2Dirty = false;
            }
        } finally {
            lock2.writeLock().unlock();
        }

    }
}
