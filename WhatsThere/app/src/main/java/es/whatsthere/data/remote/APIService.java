package es.whatsthere.data.remote;

import es.whatsthere.data.model.MessageResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIService {

    @Multipart
    @POST("/recon")
    Call<MessageResponse> uploadData(@Part MultipartBody.Part image);
}
