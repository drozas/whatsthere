package es.whatsthere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.speech.tts.TextToSpeech;

import java.io.FileInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Button cameraButton, galleryButton, descriptionButton;
    private ImageView selectedImage;
    private String currentPhotoPath;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        descriptionButton = findViewById(R.id.descriptionButton);
        selectedImage = findViewById(R.id.selectedImage);

        cameraButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        descriptionButton.setOnClickListener(this);


        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(new Locale("es", "ES"));
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraButton:

                dispatchTakePictureIntent();

                break;

            case R.id.galleryButton:

                Intent selectFromGalleryIntent = new Intent(Intent.ACTION_PICK);
                selectFromGalleryIntent.setType("image/*");
                if (selectFromGalleryIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(selectFromGalleryIntent, REQUEST_IMAGE_GALLERY);
                }

                break;

            case R.id.descriptionButton:
                if (selectedImage != null) {
                    // TODO: Llamada REST a API
                    restCall();
                    String texto = "Texto prueba";
                    ConvertTextToSpeech(texto);

                } else {
                    // TODO
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri;

        if (requestCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    imageUri = data.getData();
                    if(imageUri != null){
                        try{
//                            Bundle extras = data.getExtras();
//                            Bitmap imageBitmap = (Bitmap) extras.get("data");
//                            selectedImage.setImageBitmap(imageBitmap);

                            setPic();
                        }
                        catch (Exception e){
                            e.printStackTrace(); // TODO manejar el posible error
                        }
                    }

                    break;
                case REQUEST_IMAGE_GALLERY:
                    imageUri = data.getData();
                    if(imageUri != null){
                        try{
                          Bitmap  currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            selectedImage.setImageBitmap(currentImage);
                        }
                        catch (Exception e){
                            e.printStackTrace(); // TODO manejar el posible error
                        }
                    }

                    break;
            }

        }
        System.out.println("CURRENT PHOTO PATH: "+currentPhotoPath);
        if(currentPhotoPath != null){
            File imgFile = new  File(currentPhotoPath);

            if(imgFile.exists()){
                try{
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ImageView myImage = (ImageView) findViewById(R.id.selectedImage);
                    myImage.setBackground(null);
                    myImage.setImageBitmap(Bitmap.createScaledBitmap(myBitmap,200,200,false));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"es.whatsthere.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = selectedImage.getWidth();
        int targetH = selectedImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        selectedImage.setImageBitmap(bitmap);
    }

    private void ConvertTextToSpeech(CharSequence text) {
        // TODO Auto-generated method stub
        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public void restCall(){
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            // server back-end URL
            HttpPost httppost = new HttpPost("http://localhost:8080/FileUploaderRESTService-1/rest/upload");
            //MultipartEntity entity = new MultipartEntity();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            /* example for setting a HttpMultipartMode */
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            /* example for adding an image part */
            FileBody fileBody = new FileBody(new File(currentPhotoPath));
            builder.addPart("my_file", fileBody);
            HttpEntity entity = builder.build();

            // set the file input stream and file name as arguments
            //entity.addPart("file", new InputStreamBody(fis, inFile.getName()));
            httppost.setEntity(entity);
            // execute the request
            HttpResponse response = httpclient.execute(httppost);

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity, "UTF-8");

            System.out.println("[" + statusCode + "] " + responseString);

        } catch (ClientProtocolException e) {
            System.err.println("Unable to make connection");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Unable to read file");
            e.printStackTrace();
        }
    }
}
