import Counter.Counter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    private static final int threadGroupSize = 10;
    private static final int numThreadGroups = 10;
    private static final int totalRequest = 100;
    private static final int delay = 2;
    public static final ConcurrentLinkedQueue<Long> latencies1 = new ConcurrentLinkedQueue<>();
    public static final ConcurrentLinkedQueue<Long> latencies2 = new ConcurrentLinkedQueue<>();
    public static void main(String[] args) throws InterruptedException {
        MultipleThread();
    }

    private static void MultipleThread() throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread[] threads = new Thread[numThreadGroups];
        Counter unsucessfulCounter = new Counter();
        for (int i = 0; i < numThreadGroups; i++){
            threads[i] = new Thread(() -> {
                Thread[] innerThreads = new Thread[threadGroupSize];
                for (int j = 0; j < threadGroupSize; j++) {
                    Runnable worker = new Worker(unsucessfulCounter, latencies1, latencies2);
                    innerThreads[j] = new Thread(worker);
                    innerThreads[j].start();
                }
                try {
                    for (int j = 0; j < threadGroupSize; j++) {
                        innerThreads[j].join();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
            Thread.sleep(delay*1000);
        }
        for (int i = 0; i < numThreadGroups; i++) {
            threads[i].join();
        }
        long end = System.currentTimeMillis();
        double wallTime = (end - start) / 1000.0;
        int totalRequestsSent = threadGroupSize * numThreadGroups * totalRequest * 4;
        int sucessfulCount = (totalRequestsSent - unsucessfulCounter.getVal());
        double throughput = totalRequestsSent / wallTime;
        System.out.println("Wall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("Successful Requests: " + sucessfulCount);
        System.out.println("Failed Requests: " + unsucessfulCounter.getVal());
        System.out.println("For post and get album: ");
        calculateAndDisplayStatistics(latencies1);
        System.out.println("For Review:");
        calculateAndDisplayStatistics(latencies2);

    }
    public static void calculateAndDisplayStatistics(ConcurrentLinkedQueue<Long> latenciesQueue) {
        List<Long> latencies = new ArrayList<>(latenciesQueue);
        Collections.sort(latencies);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        double median = latencies.size() % 2 == 0 ?
                (latencies.get(latencies.size() / 2) + latencies.get(latencies.size() / 2 - 1)) / 2.0 :
                latencies.get(latencies.size() / 2);
        double average = latencies.stream().mapToLong(val -> val).average().orElse(0.0);
        long p99 = latencies.get((int) (latencies.size() * 0.99));

        System.out.println("Min Latency: " + min + " ms");
        System.out.println("Max Latency: " + max + " ms");
        System.out.println("Median Latency: " + median + " ms");
        System.out.println("Average Latency: " + average + " ms");
        System.out.println("99th Percentile Latency: " + p99 + " ms");
    }

}
