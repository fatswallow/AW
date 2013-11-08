
package com.example.effects;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.BassBoost;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.effects.R;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Effects extends Activity {
    
    private AudioTrack at; 
    private FileInputStream fin;
    private DataInputStream dis;
    private String path = "/data/a.wav";
    private byte[] s1;
    private int bufferSize = 512;
    private Thread t;
    private boolean isRunning = true;
    private LinearLayout mLayout;
    
    private BassBoost mBass;
    //private SeekBar fSlider;
    //private short bassStrength = 0;
    
    private Visualizer mVisualizer;
    private static final float VISUALIZER_HEIGHT_DIP = 500f;
    private VisualizerView mVisualizerView;

    private LoudnessEnhancer mLoudness;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_loudness);
        
        mLayout = new LinearLayout(this);
        setContentView(mLayout);
  
        int minBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        
        Log.d("abao", "new AudioTrack");
        
        at = new AudioTrack(AudioManager.STREAM_RING, 44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize,
                AudioTrack.MODE_STREAM);

        try {
            fin = new FileInputStream(path);
            dis = new DataInputStream(fin);
            
            Log.d("abao", "play");
            at.play();    
            s1 = new byte[bufferSize];

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        setupBassBoostAndUi();
        setupLoudnessEnhancerAndUi();
        setupVisualizerFxAndUi();
        
        t = new Thread() {

            @Override
            public void run() {
                super.run();
                int i1;
                Log.d("abao", "write begin");
                try {
                    while (isRunning && ((i1 = dis.read(s1, 0, bufferSize)) > -1))
                    {
                        if (i1 > -1)
                            at.write(s1, 0, i1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("abao", "write end");
            }
            
        };
        t.start();
        
        mBass.setEnabled(true);
        mVisualizer.setEnabled(true);
        mLoudness.setEnabled(true);
    }

    private void setupBassBoostAndUi()
    {
        mBass = new BassBoost(0, at.getAudioSessionId());
        
        TextView tv = new TextView(this);
        tv.setText("Bass Boost");
        mLayout.addView(tv);

        SeekBar fSlider = new SeekBar(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.addView(fSlider);
        
        fSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
                short bassStrength = (short) (1000 * progress / seekBar.getMax());
                Log.d("abao", "bassStrength=" + bassStrength);
                mBass.setStrength(bassStrength);
            }
        });
    }
    private void setupLoudnessEnhancerAndUi()
    {
        mLoudness = new LoudnessEnhancer(at.getAudioSessionId());
        
        TextView tv = new TextView(this);
        tv.setText("Loudness Enhancer");
        mLayout.addView(tv);

        SeekBar fSlider = new SeekBar(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.addView(fSlider);
        
        fSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
                int targetGainmB = (short) (1000 * progress / seekBar.getMax());
                Log.d("abao", "targetGainmB=" + targetGainmB);
                mLoudness.setTargetGain(targetGainmB);
            }
        });
    }
    private void setupVisualizerFxAndUi()
    {

        TextView tv = new TextView(this);
        tv.setText("Visualizer");
        mLayout.addView(tv);
        
        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources()
                        .getDisplayMetrics().density)));
        mLayout.addView(mVisualizerView);

        mVisualizer = new Visualizer(at.getAudioSessionId());
        // 参数内必须是2的位数
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        // 设置允许波形表示，并且捕获它
        mVisualizer.setDataCaptureListener(new OnDataCaptureListener()
        {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer,
                    byte[] waveform, int samplingRate)
            {
                mVisualizerView.updateVisualizer(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft,
                    int samplingRate)
            {
                mVisualizerView.updateVisualizer(fft);
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loudness, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t = null;
        mBass.release();
        mVisualizer.release();
        mLoudness.release();
        at.release();
    }
    
    class VisualizerView extends View
    {

        private byte[] mBytes;
        private float[] mPoints;
        // 矩形区域
        private Rect mRect = new Rect();
        // 画笔
        private Paint mPaint = new Paint();

        // 初始化画笔
        private void init()
        {
            mBytes = null;
            mPaint.setStrokeWidth(1f);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLUE);
        }

        public VisualizerView(Context context)
        {
            super(context);
            init();
        }

        public void updateVisualizer(byte[] mbyte)
        {
            mBytes = mbyte;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            if (mBytes == null)
            {
                return;
            }
            if (mPoints == null || mPoints.length < mBytes.length * 4)
            {
                mPoints = new float[mBytes.length * 4];
            }
            
            mRect.set(0, 0, getWidth(), getHeight());

            for (int i = 0; i < mBytes.length - 1; i++)
            {
                mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
                mPoints[i * 4 + 1] = mRect.height() / 2
                        + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2)
                        / 128;
                mPoints[i * 4 + 2] = mRect.width() * (i + 1)
                        / (mBytes.length - 1);
                mPoints[i * 4 + 3] = mRect.height() / 2
                        + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2)
                        / 128;
            }

            canvas.drawLines(mPoints, mPaint);
        }
    }
}
