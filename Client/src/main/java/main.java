import Counter.Counter;
import io.swagger.client.ApiException;

import java.util.concurrent.CountDownLatch;

public class main {
    private static final int threadGroupSize = 10;
    private static final int numThreadGroups = 10;
    private static final int totalRequest = 100;
    private static final int delay = 2;

    public main(String[] args) throws ApiException, InterruptedException {
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
                    Runnable worker = new Worker(unsucessfulCounter);
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
        long end = System.currentTimeMillis();
        double wallTime = (end - start) / 1000.0;
        int totalRequestsSent = threadGroupSize * numThreadGroups * totalRequest;
        int sucessfulCount = (totalRequestsSent - unsucessfulCounter.getVal());
        double throughput = totalRequestsSent / wallTime;
        System.out.println("Wall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("Successful Requests: " + sucessfulCount);
        System.out.println("Failed Requests: " + unsucessfulCounter.getVal());
    }

}
