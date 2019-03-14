package es.whatsthere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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

        clientAmazon = new AmazonRekognitionClient();
        clientAmazon.setRegion(Region.getRegion("eu-west-1"));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraButton:

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

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
                            currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            selectedImage.setImageBitmap(currentImage);
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
                            currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
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
}
