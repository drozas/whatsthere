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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Button cameraButton, galleryButton, descriptionButton;
    private ImageView selectedImage;
    private String currentPhotoPath;
    private Bitmap currentImage;
    private AmazonRekognitionClient clientAmazon;

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

        clientAmazon = new AmazonRekognitionClient(new BasicAWSCredentials("", ""));
        clientAmazon.setRegion(Region.getRegion("eu-west-1"));

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

                    DetectLabelsRequest request = new DetectLabelsRequest(image);

                    DetectLabelsResult result = clientAmazon.detectLabels(request);

                    List <Label> = result.getLabels();
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

}
