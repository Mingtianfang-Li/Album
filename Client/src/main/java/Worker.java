
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
    private ConcurrentLinkedQueue<Long> latencies1;
    private ConcurrentLinkedQueue<Long> latencies2;

    public Worker( Counter unsucesssfulCounter, ConcurrentLinkedQueue<Long> latencies1, ConcurrentLinkedQueue<Long> latencies2) {
        this.albumProfile = new AlbumsProfile();
        albumProfile.setArtist("artist");
        albumProfile.setTitle("title");
        albumProfile.setYear("year");
        this.unsucesssfulCounter = unsucesssfulCounter;
        this.apiInstance = apiSetUp();
        this.likeApiInstance = reviewApiSetup();
        this.latencies1 = latencies1;
        this.latencies2 = latencies2;
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
        long latency1 = System.currentTimeMillis();
        latencies11.add(latency1 - start);
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
        long latency2 = System.currentTimeMillis();
        latencies11.add(latency2 - latency1);
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
        long latency3 = System.currentTimeMillis();
        latencies22.add(latency3 - latency2);
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
        long latency4 = System.currentTimeMillis();
        latencies22.add(latency4 - latency3);
        retries = 0;
        do {
            try {
                retry = false;
                likeApiInstance.review("dislike", albumID);
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES){
            unsucesssfulCounter.inc();
        }
        long latency5 = System.currentTimeMillis();
        latencies22.add(latency5 - latency4);
        retries = 0;
        do {
            try {
                retry = false;
                String likeID = generateAlbumID(albumID);
                Likes lk = likeApiInstance.getLikes(likeID);
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES){
            unsucesssfulCounter.inc();
        }
        long latency6 = System.currentTimeMillis();
        latencies22.add(latency6 - latency5);
    }

    private String generateAlbumID(String number){
        Random rand = new Random();
        int n = rand.nextInt(Integer.parseInt(number)) + 1;
        return Integer.toString(n);
    }

}
