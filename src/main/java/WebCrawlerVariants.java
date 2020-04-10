import java.util.*;
import java.util.concurrent.*;

public class WebCrawlerVariants {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Map<Integer, List<Integer>> nextUrls;
    private final Queue<Integer> queue = new LinkedBlockingQueue<>();
    int count = 0;

    public WebCrawlerVariants(Map<Integer, List<Integer>> nextUrls) {
        this.nextUrls = nextUrls;
    }

    public static void main(String[] args) {
        final Map<Integer, List<Integer>> nextUrls = new HashMap<>();
        nextUrls.put(9, Arrays.asList(10));
        nextUrls.put(7, Arrays.asList(11, 12, 13));
        nextUrls.put(2, Arrays.asList(6, 4, 5));
        nextUrls.put(3, Arrays.asList(7));
        nextUrls.put(8, Arrays.asList(9));
        nextUrls.put(1, Arrays.asList(2, 3, 8));

        final WebCrawlerVariants webCrawler = new WebCrawlerVariants(nextUrls);
//        final ForkJoinPool forkJoinPool = new ForkJoinPool(10);
//        forkJoinPool.invoke(new RecursiveWebCrawlerTask(1, nextUrls));

//        webCrawler.crawlSingleThread(1);
//        System.out.println("start");

        webCrawler.crawlMultiThreaded(1);
//        System.out.println("end");
        webCrawler.shutdown();
    }

    public void shutdown() {
        executor.shutdown();
    }

    private List<Integer> getNextUrls(final Integer url) {
        try {
            Thread.sleep(6000);
            return nextUrls.get(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void crawlSingleThread(final Integer url) {
        System.out.println("visited " + url + " at " + new Date());
        if (!nextUrls.containsKey(url))
            return;
        final List<Integer> neighbors = getNextUrls(url);
        for (final Integer nexturl : neighbors) {
            crawlSingleThread(nexturl);
        }
    }


    public void crawlMultiThreaded(final Integer url) {
        System.out.println("visited " + url + " at " + new Date());
        if (!nextUrls.containsKey(url))
            return;
        final List<Integer> neighbors = getNextUrls(url);
        List<Future<?>> f = new ArrayList<>();
        for (final Integer nexturl : neighbors) {
            Future<?> tmp = executor.submit(() -> {
                crawlMultiThreaded(nexturl);
            });
            f.add(tmp);
        }
        for (int i = 0; i < neighbors.size(); i++) {
            try {
                f.get(i).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void crawlMultiThreaded1(final Integer url) {
        System.out.println("visited " + url + " at " + new Date());
        if (!nextUrls.containsKey(url))
            return;

        executor.submit(() -> {
            final List<Integer> neighbors = getNextUrls(url);
            for (final Integer nexturl : neighbors) {
                crawlMultiThreaded(nexturl);
            }
        });

    }

    public void crawlMultiThreaded2(final Integer url) {

    }

    private static class RecursiveWebCrawlerTask extends RecursiveAction {
        final Integer url;
        private final Map<Integer, List<Integer>> nextUrls;

        private RecursiveWebCrawlerTask(Integer url, final Map<Integer, List<Integer>> nextUrls) {
            this.url = url;
            this.nextUrls = nextUrls;
        }

        @Override
        protected void compute() {
            System.out.println("visited " + url + " at " + new Date());
            if (!nextUrls.containsKey(url))
                return;

            final List<Integer> neighbors = getNextUrls(url);
            final List<RecursiveWebCrawlerTask> tasks = new ArrayList<>();
            for (final Integer nexturl : neighbors) {
                final RecursiveWebCrawlerTask rt = new RecursiveWebCrawlerTask(nexturl, nextUrls);
                rt.fork();
                tasks.add(rt);
            }

            for (final RecursiveWebCrawlerTask rt : tasks) {
                rt.join();
            }
        }

        private List<Integer> getNextUrls(final Integer url) {
            try {
                Thread.sleep(6000);
                return nextUrls.get(url);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

}
