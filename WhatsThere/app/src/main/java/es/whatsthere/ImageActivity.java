package es.whatsthere;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Button cameraButton, galleryButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);

        cameraButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cameraButton:

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

                break;

            case R.id.galleryButton:

/**                Intent selectFromGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                if (selectFromGalleryIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(selectFromGalleryIntent, REQUEST_IMAGE_GALLERY);
                }
**/
                break;
        }
    }
}
