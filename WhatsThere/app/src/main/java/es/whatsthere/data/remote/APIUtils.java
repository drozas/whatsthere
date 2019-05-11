package es.whatsthere.data.remote;

public class APIUtils {

    private APIUtils() {}

    public static final String BASE_URL = "http://34.90.24.30:5500";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}

