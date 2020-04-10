import javafx.util.Pair;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ScheduledeExecutorService {

    public static void main(String[] args) {
        final ExecutorService executorService = new ExecutorService();
        executorService.submit(() -> {
            System.out.println("task 1");
        },5);
        executorService.submit(() -> {
            System.out.println("task 2");
        }, 3);

        executorService.submit(() -> {
            System.out.println("task 3");
        }, 2);

        executorService.submit(() -> {
            System.out.println("task 4");

        }, 1);
        executorService.shutDown();
    }

    private static class ExecutorService {
        private final PriorityQueue<Pair<Runnable, Long>> queue = new PriorityQueue<>(Comparator.comparing(Pair::getValue));
        private volatile boolean shutdown = false;
        private long currentSleepInMillis = Long.MAX_VALUE;
        private final Thread scheduler = getScheduler();

        public ExecutorService() {
            scheduler.start();
        }

        public void submit(final Runnable task, int delayInSeconds) {
            if(shutdown)
                return;
            final long scheduledTimestamp = System.currentTimeMillis() + delayInSeconds * 1000;
            queue.add(new Pair<>(task, scheduledTimestamp));
            scheduler.interrupt();
        }

        private final Thread getScheduler() {
            final Thread scheduler = new Thread(() -> {
                while (!shutdown || !queue.isEmpty()) {
                    try {
                        Thread.sleep(currentSleepInMillis);
                    } catch (InterruptedException e) {
                    }
                    final Pair<Runnable, Long> top = queue.peek();
                    final long currentTime = System.currentTimeMillis();
                    if (currentTime >=  top.getValue()) {
                        queue.poll().getKey().run();
                    }
                    currentSleepInMillis = queue.isEmpty() ? Long.MAX_VALUE : queue.peek().getValue() - currentTime;
                }
            });
            return scheduler;
        }

        void shutDown() {
            shutdown = true;
            currentSleepInMillis = 0;
        }
    }
}
