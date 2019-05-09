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
import org.json.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.speech.tts.TextToSpeech;
import android.os.AsyncTask;
import java.io.*;
import java.net.*;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.util.JsonReader;



public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Button cameraButton, galleryButton, descriptionButton;
    private ImageView selectedImage;
    private String currentPhotoPath;
    TextToSpeech tts;
    Bitmap bitmap;

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
                    String response = "{\"status\":\"ok\", \"result\":[{\"class\":\"taza\",\"p\":\"90\"},{\"class\":\"colacao\",\"p\":\"90\"}"+ ",{\"class\":\"taza\",\"p\":\"90\"}]}";

                    String texto = parseResponse(response).toString();
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

        if (resultCode == RESULT_OK) {
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

        bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        selectedImage.setImageBitmap(bitmap);
    }

    private void ConvertTextToSpeech(CharSequence text) {
        // TODO Auto-generated method stub
        //String response = "{\"status\":\"ok\", \"result\":[{\"class\":\"taza\",\"p\":\"70\"},{\"class\":\"colacao\",\"p\":\"90\"}"+ ",{\"class\":\"taza\",\"p\":\"90\"}]}";

        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public void restCall(){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    // Create URL
                    URL githubEndpoint = new URL("http://34.90.58.90:5500/recon");
                    //URL githubEndpoint = new URL("http://192.168.43.60:5500");
                    //URL githubEndpoint = new URL("https://services5.arcgis.com/UxADft6QPcvFyDU1/arcgis/rest/services/Red_Metro/FeatureServer/0/query?where=1%3D1&outFields=*&outSR=4326&f=json");
                    StringBuilder result = new StringBuilder();
                    // Create connection
                    HttpURLConnection myConnection = (HttpURLConnection) githubEndpoint.openConnection();
                    myConnection.setRequestMethod("POST");
                    //myConnection.setRequestProperty("content-type", "application/json");
                    myConnection.setReadTimeout(60*1000);
                    myConnection.setRequestProperty("Content-Type", "multipart/form-data;file=" + currentPhotoPath);

                    myConnection.connect();

                    OutputStream output = myConnection.getOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, output);
                    output.close();
                    //DataOutputStream request = new DataOutputStream(myConnection.getOutputStream());

                    /*request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" +
                            this.attachmentName + "\";filename=\"" +
                            this.attachmentFileName + "\"" + this.crlf);*/
                    /*request.writeBytes(currentPhotoPath);
                    byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
                    for (int i = 0; i < bitmap.getWidth(); ++i) {
                        for (int j = 0; j < bitmap.getHeight(); ++j) {
                            //we're interested only in the MSB of the first byte,
                            //since the other 3 bytes are identical for B&W images
                            pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
                        }
                    }

                    request.write(pixels);*/


                    //request.flush();
                    //request.close();
                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        InputStream in = new BufferedInputStream(myConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        System.out.println(result.toString());

                    }
                    else {
                        // Error handling code goes here
                        System.out.println("ERROR");
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });

    }

    public StringBuilder parseResponse(String response){
        StringBuilder texto = new StringBuilder();

        try {
            JSONObject json = new JSONObject(response);
            JSONArray jsonArray = json.getJSONArray("result");


            HashMap<String, Integer> data = new HashMap<String, Integer>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                if (jsonData.getInt("p") >= 85) {
                    String clave = jsonData.getString("class");
                    if (data.containsKey(clave)) {
                        data.put(clave, data.get(clave) + 1);
                    } else {
                        data.put(clave, 1);
                    }
                }
            }


        for(String objeto : data.keySet()){
            texto.append("En la imagen hay " + data.get(objeto) + " " + objeto + ".\n");
        }
        }catch (Exception e){
            e.printStackTrace();
        }
        return texto;

    }
}
