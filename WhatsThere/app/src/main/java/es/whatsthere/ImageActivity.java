package es.whatsthere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import es.whatsthere.data.model.MessageResponse;
import es.whatsthere.data.remote.APIService;
import es.whatsthere.data.remote.APIUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Bitmap myBitmap;
    private APIService mAPIService;
    ProgressDialog pDialog;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button submitBtn = findViewById(R.id.descriptionButton);

        mAPIService = APIUtils.getAPIService();

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(new Locale("en", "GB"));
                }
            }
        });


        //Button Press
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(myBitmap != null){
                    MultipartBody.Part profilePic = null;
                    File file = CreateFileFromBitmap(myBitmap);

                    RequestBody requestFile = RequestBody.create(
                            MediaType.parse("multipart/form-data"), file);

                    profilePic = MultipartBody.Part.createFormData("file", "file.jpg", requestFile);

                    sendPost(profilePic);
                    Log.d(TAG, "Enviando imagen");
                    //ConvertTextToSpeech("Picture description requested.");
                }else{
                    ConvertTextToSpeech("There is no picture selected.");
                }
            }
        });
    }

    public void sendPost(MultipartBody.Part imagePart) {
        mAPIService.uploadData(imagePart).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                Log.e(TAG,String.valueOf(response.code()));
                if(response.isSuccessful()) {
                    Log.d(TAG,"Response successful!");
                    Log.d(TAG, "post submitted to API." + response.body().toString());

                    Gson gson = new Gson();
                    final String successResponse = gson.toJson(response.body());
                    final StringBuilder stringBuilder = parseResponse(successResponse);
                    Log.d(TAG, "Valor de String: " + stringBuilder);

                    ConvertTextToSpeech("The picture contains " + stringBuilder.toString());

                }
                else{
                    String text = "Error: " + response.code() +  " \nImage sending error";
                    ConvertTextToSpeech(text);
                    Toast toast = Toast.makeText(ImageActivity.this, text,Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast toast = Toast.makeText(ImageActivity.this, "Unable to send image " + t.getCause(),Toast.LENGTH_LONG);
                toast.show();
                Log.e(TAG, t.getMessage().toLowerCase());
                Log.e(TAG,call.request().body().toString());
                Log.e(TAG,call.request().url().toString());
            }
        });
    }

    public StringBuilder parseResponse(String response){
        Log.d(TAG, "Texto: " + response+"\n\n\n\n");
        StringBuilder texto = new StringBuilder();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray jsonArray = json.getJSONArray("result");
            HashMap<String, Integer> data = new HashMap<>();

            if(jsonArray.length()> 0){
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    if (jsonData.getInt("p") >= 10) {
                        String clave = jsonData.getString("class");
                        if (data.containsKey(clave)) {
                            data.put(clave, data.get(clave) + 1);
                        } else {
                            data.put(clave, 1);
                        }
                    }
                }

                for(String objeto : data.keySet()){
                    texto.append(data.get(objeto) + " " + objeto + ".\n");
                }
            }else{
                texto.append("The system could not recognize any object in this picture.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return texto;
    }

    private void ConvertTextToSpeech(CharSequence text) {
        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public File CreateFileFromBitmap(Bitmap bitmap){
        //create a file to write bitmap data
        File f = new File(this.getCacheDir(), "childimage");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public void takeUserImage(View view) {
        ConvertTextToSpeech("Activated camera.");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {

            if (resultCode == RESULT_OK) {

                myBitmap = data.getExtras().getParcelable("data");
                ImageView image = findViewById(R.id.userProfileImage);
                image.setImageBitmap(myBitmap);

                ConvertTextToSpeech("Picture taken successfully.");
            }
        }
    }
}
