
import Counter.Counter;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.io.File;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Worker implements Runnable{
    final static private String url = "http://localhost:8080/HW2Server_war_exploded"; // server url
    final static private int MAX_RETRIES = 3;
    final static private String fileUrl = "nmtb.png";
    private File imagefile;

    private Counter unsucesssfulCounter;

    private DefaultApi apiInstance;
    private static final int totalRequests = 100;

    private AlbumsProfile albumProfile;

    public Worker( Counter unsucesssfulCounter) {
        this.albumProfile = new AlbumsProfile();
        albumProfile.setArtist("artist");
        albumProfile.setTitle("title");
        albumProfile.setYear("year");
        this.unsucesssfulCounter = unsucesssfulCounter;
        this.apiInstance = apiSetUp();
    }

    public DefaultApi apiSetUp() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(url);
        return new DefaultApi(apiClient);
    }

    @Override
    public void run() { fun1();}

    private void fun1() {
        imagefile = new File(fileUrl);
        for (int i = 0; i <  totalRequests; i++){
            try {
                requestWithRetry();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void requestWithRetry() {
        int retries = 0;
        boolean retry;
        String albumID = generateAlbumID();
        do {
            try {
                retry = false;
                apiInstance.newAlbum(imagefile, albumProfile);
            } catch (ApiException e) {
                retries += 1;
                retry = true;
                e.printStackTrace();
            }
        } while ( retry && (retries < MAX_RETRIES));
        if (retries == MAX_RETRIES) {
            unsucesssfulCounter.inc();
        }
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
    }

    private String generateAlbumID(){
        return "1";
        /*
        Random r = new Random();
        int id = r.nextInt(100) + 1;
        return String.valueOf(id);

         */
    }
}
