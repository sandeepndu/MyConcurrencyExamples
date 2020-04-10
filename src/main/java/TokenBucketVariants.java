import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketVariants {
    public static void main(String[] args) throws InterruptedException {
        final TokenBucketWithoutExecutorBlocking tokenBucket = new TokenBucketWithoutExecutorBlocking(1, 10);
//        tokenBucket.start();

        final Random rn = new Random();
        final ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                while (true) {
//                    Thread.sleep(1000);
                    tokenBucket.getTokens(rn.nextInt(10) + 1);

//                    if (tokenBucket.getTokens(2)) {
//                        System.out.println("taken tokens");
//                    } else {
//                        System.out.println("no tokens available");
//                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    public static class TokenBucketWithoutExecutorBlocking {
        final long rateOfIncomingTokensPerSecond;
        final int bucketCapacity;
        long tokenCount = 0;
        private long lastUpdatedTimeStamp = System.currentTimeMillis();

        public TokenBucketWithoutExecutorBlocking(long rateOfIncomingTokensPerSecond, int bucketCapacity) {
            this.rateOfIncomingTokensPerSecond = rateOfIncomingTokensPerSecond;
            this.bucketCapacity = bucketCapacity;
        }

        // higher n value might lead to starvation
        public void getTokens(int n) throws InterruptedException {
            while (true) {
                synchronized (this) {
                    final long currentTime = System.currentTimeMillis();
                    tokenCount = rateOfIncomingTokensPerSecond * (currentTime - lastUpdatedTimeStamp);
                    lastUpdatedTimeStamp = currentTime;
                    if (tokenCount >= n) {
                        tokenCount -= n;
                        System.out.println("taken " + n + " tokens");
                        return;
                    }
                }
                final long timeToWaitInMillis = ((n - tokenCount) / rateOfIncomingTokensPerSecond)*1000;
                System.out.println("Insufficient Tokens, waiting for " + timeToWaitInMillis + " ms");
                Thread.sleep(timeToWaitInMillis);
            }
        }

        private long getTokenCount() {
            return Math.min(bucketCapacity, tokenCount + (System.currentTimeMillis() - lastUpdatedTimeStamp) * rateOfIncomingTokensPerSecond);
        }

    }

    public static class TokenBucketWithExecutorBlocking {
        final long rateOfIncomingTokensPerSecond;
        final int bucketCapacity;
        private final Lock lock = new ReentrantLock();
        private final Condition sufficientTokens = lock.newCondition();
        private final Condition bucketNotFull = lock.newCondition();
        private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        private long currentTokenCount = 0;

        public TokenBucketWithExecutorBlocking(long rateOfIncomingTokensPerSecond, int bucketCapacity) {
            this.rateOfIncomingTokensPerSecond = rateOfIncomingTokensPerSecond;
            this.bucketCapacity = bucketCapacity;
        }

        public void start() {
            executorService.scheduleAtFixedRate(() -> {
                lock.lock();
                try {
                    while (currentTokenCount >= bucketCapacity) {
                        bucketNotFull.await();
                    }
                    currentTokenCount++;
                    System.out.println("current token count" + currentTokenCount);
                    sufficientTokens.signalAll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }, 0, rateOfIncomingTokensPerSecond, TimeUnit.SECONDS);

        }


        // higher n value might lead to starvation
        public void getTokens(int n) throws InterruptedException {
            lock.lock();
            try {
                while (n > currentTokenCount) {
                    sufficientTokens.await();
                }
                currentTokenCount -= n;
                System.out.println("taken " + n + " tokens, currentToken count " + currentTokenCount);
                bucketNotFull.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public static class TokenBucketWithoutExecutorNonBlocking {
        private final long rateOfIncomingTokensPerSecond;
        private final int bucketCapacity;
        private long currentTokens = 0;
        private long lastUpdatedTime = System.currentTimeMillis();

        public TokenBucketWithoutExecutorNonBlocking(long rateOfIncomingTokens, int bucketCapacity) {
            this.rateOfIncomingTokensPerSecond = rateOfIncomingTokens;
            this.bucketCapacity = bucketCapacity;
        }

        public synchronized boolean getTokens(int n) {
            //non blocking getTokens
            final long currentTime = System.currentTimeMillis();
            currentTokens = Math.min(bucketCapacity, currentTokens + rateOfIncomingTokensPerSecond * (currentTime - lastUpdatedTime));
            lastUpdatedTime = currentTime;
            if (currentTokens >= n) {
                currentTokens -= n;
                return true;
            }
            return false;
        }

    }

    public static class TokenBucketNonBlocking {
        private final long rateOfIncomingTokensPerSecond;
        private final int bucketCapacity;
        private final AtomicInteger tokenCount = new AtomicInteger(0);
        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        public TokenBucketNonBlocking(long rateOfIncomingTokens, int bucketCapacity) {
            this.rateOfIncomingTokensPerSecond = rateOfIncomingTokens;
            this.bucketCapacity = bucketCapacity;
        }

        public void start() {
            executor.scheduleAtFixedRate(() -> {
                tokenCount.updateAndGet(value -> value < bucketCapacity ? value + 1 : value);
            }, 0, rateOfIncomingTokensPerSecond, TimeUnit.SECONDS);
        }

        public boolean getTokens(int n) {
            //non blocking getTokens
            final int tokens = tokenCount.getAndUpdate(value -> value >= n ? value - n : value);
            return tokens >= n;
        }
    }
}
