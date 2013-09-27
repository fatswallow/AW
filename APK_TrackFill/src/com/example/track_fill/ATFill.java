
package com.example.track_fill;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ATFill extends Activity {
    
    private final String TAG = "ATFill";
    private AudioTrack mTrack;
    private EditText mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atfill);
        
        Button btn1 = (Button)findViewById(R.id.button1);
        btn1.setText("new Track");
        btn1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                int minBufferSize = AudioTrack.getMinBufferSize(48000,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT);
                mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize,
                        AudioTrack.MODE_STREAM);
                mTrack.play();
            }
        });
        
        Button btn2 = (Button)findViewById(R.id.button2);
        btn2.setText("del Track");
        btn2.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mTrack.stop();
                mTrack.release();
            }
        });
        
        Button btn3 = (Button)findViewById(R.id.button3);
        mEdit = (EditText)findViewById(R.id.editText1);
        btn3.setText("fill Track");
        btn3.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String text = mEdit.getText().toString();
                int len = Integer.parseInt(text);
                Log.d(TAG, "text=" + len);
                byte[] buf = new byte[len];
                mTrack.write(buf, 0, len);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.atfill, menu);
        return true;
    }

}
