
package com.example.tts_demo;

import android.os.Bundle;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

public class TTSDemo extends Activity implements OnInitListener {
    
    private static final String TAG = "TTS Demo";
    private EditText inputText = null;
    private Button speakBtn = null;
    private TextToSpeech mTts;
    //private static final int REQ_TTS_STATUS_CHECK = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputText = (EditText)findViewById(R.id.editText1);
        speakBtn = (Button)findViewById(R.id.button1);
        inputText.setText("This is an example of speech synthesis.");
        speakBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "onClick()");
                mTts.speak(inputText.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                mTts.synthesizeToFile(inputText.getText().toString(), null, "/mnt/sdcard/TTS.wav");
            }
        });
        mTts = new TextToSpeech(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTts != null) {
            mTts.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.shutdown();
            mTts = null;
        }
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS)
        {
            int result = mTts.setLanguage(Locale.US);

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.v(TAG, "Language is not available");
                speakBtn.setEnabled(false);
            }
            else
            {
                mTts.speak("This is an example of speech synthesis.", TextToSpeech.QUEUE_ADD, null);
                speakBtn.setEnabled(true);
            }
        }
    }
}
