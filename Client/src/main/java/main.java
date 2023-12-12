import Counter.Counter;
import io.swagger.client.ApiException;

import java.util.concurrent.CountDownLatch;

public class main {
    private static final int threadGroupSize = 10;
    private static final int numThreadGroups = 10;
    private static final int totalRequest = 100;

    public static void main(String[] args) throws ApiException, InterruptedException {
        MultipleThread();
    }

    private static void MultipleThread(){
        long start = System.currentTimeMillis();
        Counter unsucessfulCounter = new Counter();
        for (int i = 0; i < numThreadGroups; i++){
            for (int j = 0; j < threadGroupSize; j++){
                Runnable worker = new Worker(unsucessfulCounter);
                Thread thread = new Thread(worker);
                thread.start();
            }
        }
        long end = System.currentTimeMillis();
        double wallTime = (end - start) / 1000.0;
        int totalRequestsSent = threadGroupSize * numThreadGroups * totalRequest;
        int sucessfulCount = (totalRequestsSent - unsucessfulCounter.getVal());
        double throughput = totalRequestsSent / wallTime;
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("Successful Requests: " + sucessfulCount);
        System.out.println("Failed Requests: " + unsucessfulCounter.getVal());
    }

}
