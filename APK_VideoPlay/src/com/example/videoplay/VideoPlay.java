
package com.example.videoplay;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class VideoPlay extends Activity {
    
    private MediaPlayer mp;
    private SurfaceView sv; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        mp=new MediaPlayer();
        sv=(SurfaceView)findViewById(R.id.surfaceView1);
        Button play=(Button)findViewById(R.id.play);
        final Button pause=(Button)findViewById(R.id.pause);
        Button stop=(Button)findViewById(R.id.stop);

        play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mp.reset();
                try {
                    mp.setDataSource("/sdcard/Movies/B069_720P_AVC_AAC_14M_50F.mp4");
                    //mp.setDataSource("/sdcard/Widevine/sintel_base_360p_1br_notp.wvm");
                    mp.setDisplay(sv.getHolder());
                    mp.prepare();
                    mp.start();
                    pause.setText("ÔÝÍ£");
                    pause.setEnabled(true);
                }catch(IllegalArgumentException e) {
                    e.printStackTrace();
                }catch(SecurityException e) {
                    e.printStackTrace();
                }catch(IllegalStateException e) {
                    e.printStackTrace();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });

        stop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.stop();
                    pause.setEnabled(false);
                }
            }
        });

        pause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.pause();
                    ((Button)v).setText("¼ÌÐø");
                }else{
                    mp.start();
                    ((Button)v).setText("ÔÝÍ£");
                }
            }
        });

        mp.setOnCompletionListener(new OnCompletionListener(){

            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(VideoPlay.this, "ÊÓÆµ²¥·ÅÍê±Ï£¡", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if(mp.isPlaying()){
            mp.stop();
        }
        mp.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.video_play, menu);
        return true;
    }

}
