
package com.example.recordplay;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity implements View.OnClickListener{

    final static String TAG = "RecordPlay";
    RecordThread mRec;
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button1:
                Log.d(TAG, "button1");
                mRec = new RecordThread();
                mRec.mContext = this; 
                mRec.start();
                break;
            case R.id.button2:
                Log.d(TAG, "button2");
                mRec.running = false;
                break;
            default:
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btn1 = (Button)findViewById(R.id.button1);
        btn1.setText("start");
        btn1.setOnClickListener(this);
        Button btn2 = (Button)findViewById(R.id.button2);
        btn2.setText("stop");
        btn2.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    class RecordThread extends Thread{
        Context mContext;
        static final int frequency = 44100;
        final int in_channels = AudioFormat.CHANNEL_OUT_STEREO;
        final int out_channels = AudioFormat.CHANNEL_OUT_STEREO;
        //static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        boolean running = true;
        @Override
        public void run() {
            //Log.d(TAG, "run");
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.setSpeakerphoneOn(true);
            int recBufSize = AudioRecord.getMinBufferSize(frequency,
                    in_channels, audioEncoding);
            int plyBufSize = AudioTrack.getMinBufferSize(frequency,
                    out_channels, audioEncoding);
            
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                    in_channels, audioEncoding, recBufSize);

            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                    out_channels, audioEncoding, plyBufSize, AudioTrack.MODE_STREAM);
            
            byte[] recBuf = new byte[recBufSize];
            audioRecord.startRecording();
            audioTrack.play();
            while(running){
                int readLen = audioRecord.read(recBuf, 0, recBufSize);
                //Log.d(TAG, "readLen=" + readLen);
                audioTrack.write(recBuf, 0, readLen);
            }
            audioTrack.stop();
            audioRecord.stop();
        }
    }

}
