package es.whatsthere;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextToSpeech tts;
    private int SETTINGS_ACTION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("theme", "light");

        if(userTheme.equals("light")){
            setTheme(R.style.AppTheme_Light);
        }else if (userTheme.equals("dark")){
            setTheme((R.style.AppTheme_Dark));
        }else{
            setTheme(R.style.AppTheme_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button imageButton = findViewById(R.id.imageButton);
        Button videoButton = findViewById(R.id.videoButton);

        imageButton.setOnClickListener(this);
        videoButton.setOnClickListener(this);

        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(new Locale("en", "GB"));
                }
            }
        });

    }

//    @Override
//    protected void onResume(){
//        recreate();
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String userTheme = preferences.getString("theme", "light");
//
//        if(userTheme.equals("light")){
//            setTheme(R.style.AppTheme_Light);
//        }else if (userTheme.equals("dark")){
//            setTheme((R.style.AppTheme_Dark));
//        }else{
//            setTheme(R.style.AppTheme_Light);
//        }
//
//        super.onResume();
//        setContentView(R.layout.activity_main);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageButton:
                ConvertTextToSpeech("Image mode selected.");

                Intent intent = new Intent(this, ImageActivity.class);
                startActivity(intent);
                break;

            case R.id.videoButton:
                ConvertTextToSpeech("Video mode selected.");
                /* Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }*/
                break;
        }
    }

    private void ConvertTextToSpeech(CharSequence text) {
        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}
