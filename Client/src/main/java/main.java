import io.swagger.client.ApiException;

public class main {
    public static void main(String[] args) throws ApiException {
        SingleThreadClient singleThreadClient = new SingleThreadClient();
        singleThreadClient.run();
    }
}
