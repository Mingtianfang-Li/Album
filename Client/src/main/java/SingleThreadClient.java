
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.io.File;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class SingleThreadClient {
    final static private String url = ""; // server url
    final static private int MAX_RETRIES = 3;
    final static private String fileUrl = "";
    private File imagefile;
    private AlbumsProfile albumProfile;
    private String albumID;
    private static final int threadGroupSize = 10;
    private static final int numThreadGroups = 10;
    private static final int totalRequests = 100;


    public DefaultApi apiSetUp() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(url);
        return new DefaultApi();
    }

    public void run() throws ApiException { fun1();}

    private void fun1() throws ApiException {
        imagefile = new File(fileUrl);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                sendRequest();
            }
        }
        long endTime = System.currentTimeMillis();

        // Calculate wall time in seconds
        double wallTime = (endTime - startTime) / 1000.0;
        System.out.println("Wall Time: " + wallTime + " seconds");
        int totalRequestsSent = numThreadGroups * threadGroupSize * totalRequests * 2;
        double throughput = totalRequestsSent / wallTime;
        System.out.println("Throughput: " + throughput + " requests/second");
    }
    private void generateAlbumID(){
        Random r = new Random();
        int id = r.nextInt(100) + 1;
        albumID = String.valueOf(id);
    }

    // just a simple send 100 request each
    private void sendRequest() throws ApiException {
        // this is using Swagger api functions
        DefaultApi apiInstance = apiSetUp();
        for (int i = 0; i < totalRequests; i++){
            albumProfile = new AlbumsProfile(); // required to generate album profile with artist name, title, year
            apiInstance.newAlbum(imagefile, albumProfile);
            generateAlbumID();
            apiInstance.getAlbumByKey(albumID);
        }
    }
}
