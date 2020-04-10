
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FixedThreadExecutorService {
    public static void main(String[] args) throws InterruptedException {
        final ExecutorService executorService = new ExecutorService(2);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Thread.sleep(1);
            executorService.submit(() -> {
                System.out.printf("task %d\n", finalI);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    System.out.println(ignored);
                }
            });
        }
//        Thread.sleep(100000);
        executorService.shutDown();
    }

    public static class ExecutorService {
        private BlockingQueue<Runnable> tasks;
        private List<Thread> threads = new ArrayList<>();
        private volatile boolean shutdown = false;

        public ExecutorService(int capacity) {
            tasks = new ArrayBlockingQueue<>(capacity);
            for (int i = 0; i < capacity; i++) {
                threads.add(new Thread(() -> {
                    while (!shutdown || !tasks.isEmpty() ) {
                        try {
                            final Runnable task = tasks.take();
                            task.run();
                        } catch (InterruptedException e) {
                            if (shutdown && tasks.isEmpty()) {
                                break;
                            }
                        }
                    }
                    System.out.println("shutting down thread");
                }));
                threads.get(i).start();
            }
        }

        public void submit( final Runnable task) {
//            tasks.add(task);
            try {
                tasks.put(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("added new taks");
        }

        public void shutDown() {
            shutdown = true;
            for (final Thread t : threads) {
                t.interrupt();
            }
        }
    }
}
