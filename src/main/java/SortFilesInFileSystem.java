import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class SortFilesInFileSystem {
    private final static ForkJoinPool forkJoinPool = new ForkJoinPool(2);

    public static void main(String[] args) {
//        final Map<Integer, List<Integer>> nextUrls = new HashMap<>();
//        nextUrls.put(9, Arrays.asList(10));
//        nextUrls.put(7, Arrays.asList(11, 12, 13));
//        nextUrls.put(2, Arrays.asList(6, 4, 5));
//        nextUrls.put(3, Arrays.asList(7));
//        nextUrls.put(8, Arrays.asList(9));
//        nextUrls.put(1, Arrays.asList(2, 3, 8));


        final MyRecursiveTask task = new MyRecursiveTask(100);
        final Integer ret =forkJoinPool.invoke(task);
        System.out.println(ret);

    }

    public static void sort() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sortFiles(Integer rootDir) {

    }

    private static class MyRecursiveTask extends RecursiveTask<Integer> {
        private final Integer i;

        public MyRecursiveTask(Integer i) {
            this.i = i;
        }

        @Override
        protected Integer compute() {
            System.out.println(i);
            if (i < 10)
                return i;
            final MyRecursiveTask t1 = new MyRecursiveTask(i / 2);
            final MyRecursiveTask t2 = new MyRecursiveTask(i / 2);
            final ForkJoinTask<Integer> j1 = t1.fork();
            final ForkJoinTask<Integer> j2 = t2.fork();
            j1.join();
            j2.join();
            return -1;
        }
    }
}
