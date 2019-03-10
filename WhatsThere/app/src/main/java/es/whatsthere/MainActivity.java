package es.whatsthere;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static final int REQUEST_VIDEO_CAPTURE = 2;

    Button imageButton, videoButton, settingButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButton = findViewById(R.id.imageButton);
        videoButton = findViewById(R.id.videoButton);
        settingButton = findViewById(R.id.settingsButton);

        imageButton.setOnClickListener(this);
        videoButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageButton:
                setContentView(R.layout.activity_image);
                break;

            case R.id.videoButton:
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }

                break;

            case R.id.settingsButton:

                break;
        }
    }
}
