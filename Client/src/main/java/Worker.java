
import Counter.Counter;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import io.swagger.client.api.LikeApi;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.*;

public class Worker implements Runnable{
    final static private String url = "http://localhost:8080/HW2Server_war_exploded"; // server url
    final static private String reviewUrl = "";
    final static private int MAX_RETRIES = 3;
    final static private String fileUrl = "nmtb.png";
    private File imagefile;

    private Counter unsucesssfulCounter;

    private DefaultApi apiInstance;
    private static final int totalRequests = 100;

    private AlbumsProfile albumProfile;
    private LikeApi likeApiInstance;
    private String albumID;
    public static final ConcurrentLinkedQueue<Long> latencies1 = new ConcurrentLinkedQueue<>();
    public static final ConcurrentLinkedQueue<Long> latencies2 = new ConcurrentLinkedQueue<>();

    public Worker( Counter unsucesssfulCounter) {
        this.albumProfile = new AlbumsProfile();
        albumProfile.setArtist("artist");
        albumProfile.setTitle("title");
        albumProfile.setYear("year");
        this.unsucesssfulCounter = unsucesssfulCounter;
        this.apiInstance = apiSetUp();
        this.likeApiInstance = reviewApiSetup();
    }

    public DefaultApi apiSetUp() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(url);
        return new DefaultApi(apiClient);
    }

    public LikeApi reviewApiSetup() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(url);
        return new LikeApi(apiClient);
    }

    @Override
    public void run() { fun1();}

    private void fun1() {
        imagefile = new File(fileUrl);
        for (int i = 0; i <  totalRequests; i++){
            List<Long> latencies11 = new ArrayList<>();
            List<Long> latencies22 = new ArrayList<>();
            try {
                requestWithRetry(latencies11, latencies22);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latencies1.addAll(latencies11);
            latencies2.addAll(latencies22);
        }

    }

    private void requestWithRetry(List<Long> latencies11, List<Long> latencies22) throws ApiException {
        int retries = 0;
        boolean retry;
        long start = System.currentTimeMillis();
        do {
            try {
                retry = false;
                ImageMetaData imageMetaData = apiInstance.newAlbum(imagefile, albumProfile);
                albumID = imageMetaData.getAlbumID();
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES) {
            unsucesssfulCounter.inc();
        }
        long latency1 = System.currentTimeMillis() - start;
        latencies11.add(latency1);
        retries = 0;
        do {
            try {
                retry = false;
                apiInstance.getAlbumByKey(albumID);
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES) {
            unsucesssfulCounter.inc();
        }
        long latency2 = System.currentTimeMillis() - latency1;
        latencies11.add(latency2);
        retries = 0;
        do {
            try {
                retry = false;
                likeApiInstance.review("like", albumID);
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES){
            unsucesssfulCounter.inc();
        }
        long latency3 = System.currentTimeMillis() - latency2;
        latencies22.add(latency3);
        retries = 0;
        do {
            try {
                retry = false;
                likeApiInstance.getLikes(albumID);
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES){
            unsucesssfulCounter.inc();
        }
        long latency4 = System.currentTimeMillis() - latency3;
        latencies22.add(latency4);
    }
    private String generateAlbumID(){
        return "1";
        /*
        Random r = new Random();
        int id = r.nextInt(100) + 1;
        return String.valueOf(id);

         */
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
