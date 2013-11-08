
package com.example.recordmode;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordMode extends Activity implements View.OnClickListener {
    
    final String TAG = "RecordMode";
    private Button btn_start_record, btn_stop_record, btn_start_play, btn_stop_play, btn_test;
    private File audioFile;
    private boolean isPlaying = false, isRecording = false;
    private RecordTask recorder;
    private PlayTask player;
    
    private int frequence = 8000;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button1:
                Log.d(TAG, "xx1");
                recorder = new RecordTask();
                recorder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.button2:
                Log.d(TAG, "xx2");
                isRecording = false;
                break;
            case R.id.button3:
                Log.d(TAG, "xx3");
                player = new PlayTask();
                player.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.button4:
                Log.d(TAG, "xx4");
                isPlaying = false;
                break;
            case R.id.button5:
                Log.d(TAG, "AcousticEchoCanceler available=" + AcousticEchoCanceler.isAvailable());
                Log.d(TAG, "NoiseSuppressor available=" + NoiseSuppressor.isAvailable());
                Log.d(TAG, "AutomaticGainControl  available=" + AutomaticGainControl.isAvailable());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mode);
        btn_start_record = (Button)findViewById(R.id.button1);
        btn_start_record.setText("start record");
        btn_start_record.setOnClickListener(this);
        btn_stop_record = (Button)findViewById(R.id.button2);
        btn_stop_record.setText("stop record");
        btn_stop_record.setOnClickListener(this);
        btn_start_play = (Button)findViewById(R.id.button3);
        btn_start_play.setText("start play");
        btn_start_play.setOnClickListener(this);
        btn_stop_play = (Button)findViewById(R.id.button4);
        btn_stop_play.setText("stop play");
        btn_stop_play.setOnClickListener(this);
        btn_test = (Button)findViewById(R.id.button5);
        btn_test.setText("test");
        btn_test.setOnClickListener(this);
        
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/abao/");
        fpath.mkdirs();//创建文件夹
        try {
            //创建临时文件,注意这里的格式为.pcm
            audioFile = File.createTempFile("recording", ".pcm", fpath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_mode, menu);
        return true;
    }

    class RecordTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                //开通输出流到指定的文件
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                Log.e(TAG, "bufferSize=" + bufferSize);
                //实例化AudioRecord
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, frequence, channelConfig, audioEncoding, bufferSize);
                Log.e(TAG, "record=" + record);
                
                int sessionId = record.getAudioSessionId();
                AutomaticGainControl agc = AutomaticGainControl.create(sessionId);
                agc.setEnabled(true);
                Log.d(TAG, "agc enabled=" + agc.getEnabled());
                AcousticEchoCanceler aec = AcousticEchoCanceler.create(sessionId);
                //aec.setEnabled(true);
                Log.d(TAG, "aec enabled=" + aec.getEnabled());
                NoiseSuppressor ns = NoiseSuppressor.create(sessionId);
                //ns.setEnabled(true);
                Log.d(TAG, "ns enabled=" + ns.getEnabled());
                //Log.d(TAG, "AGC created");
                
                //定义缓冲
                short[] buffer = new short[bufferSize];
 
                //开始录制
                record.startRecording();
 
                int r = 0; //存储录制进度
                Log.e(TAG, "record start");
                //定义循环，根据isRecording的值来判断是否继续录制
                while(isRecording){
                    //从bufferSize中读取字节，返回读取的short个数
                    //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    //循环将buffer中的音频数据写入到OutputStream中
                    for(int i=0; i<bufferReadResult; i++){
                        dos.writeShort(buffer[i]);
                    }
                    //publishProgress(new Integer(r)); //向UI线程报告当前进度
                    r++; //自增进度值
                }
                //录制结束
                record.stop();
                Log.v("The DOS available:", "::"+audioFile.length());
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }
    
    class PlayTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(frequence, channelConfig, audioEncoding);
            short[] buffer = new short[bufferSize/4];
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
                //实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                Log.e(TAG, "play start");
                while(isPlaying && dis.available()>0){
                    int i = 0;
                    while(dis.available()>0 && i<buffer.length){
                        buffer[i] = dis.readShort();
                        i++;
                    }
                    //然后将数据写入到AudioTrack中
                    track.write(buffer, 0, buffer.length);
 
                }
 
                //播放结束
                track.stop();
                dis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }

}
