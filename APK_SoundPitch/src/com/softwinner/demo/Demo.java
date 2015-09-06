package com.softwinner.demo;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Demo extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("hello.");
		doPitch();
		doMix();
        setContentView(tv);
    }

	private void doPitch() {
		BufferedInputStream bis;
		BufferedOutputStream bos;
		byte[] in = new byte[2048];
		byte[] out = new byte[2048*16]; /* must be enough */
		int bytesread = 0;
		int framesread;
		int frameswrite;
		SoundPitch.initSound(48000, 2, -40);
		try {
			bis = new BufferedInputStream(new FileInputStream("/sdcard/in.pcm"));
			bos = new BufferedOutputStream(new FileOutputStream("/sdcard/out.pcm"));

			while ((bytesread=bis.read(in, 0, 2048)) != -1) {
				framesread = bytesread/2/2; /* stereo 16bit */
				Log.d("demo", "process(bytes=" + bytesread + ",framesread=" + framesread + ")");
				frameswrite = SoundPitch.processSound(in, out, framesread);
				bos.write(out, 0, frameswrite*2*2);
			}
			
			bis.close();
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}

	private void doMix() {
		BufferedInputStream bis;
		BufferedOutputStream bos;
		byte[] in = new byte[2048];
		byte[] out = new byte[2048];
		short[] arrin = new short[1024];
		short[] arrout = new short[1024];
		int bytesread = 0;
		int framesread;
		try {
			bis = new BufferedInputStream(new FileInputStream("/sdcard/mixin.pcm"));
			bos = new BufferedOutputStream(new FileOutputStream("/sdcard/mixout.pcm"));

			while ((bytesread=bis.read(in, 0, 2048)) != -1) {
				for (int i = 0; i < bytesread/2; ++i)
					arrin[i] = (short)((in[i*2]&0xff)|(in[i*2+1]<<8));
				SoundMix.mix(arrin, arrout, bytesread/2);
				for (int i = 0; i < bytesread/2; ++i) {
					out[i*2] = (byte)(arrout[i]&0xff);
					out[i*2+1] = (byte)((arrout[i]>>8)&0xff);
				}
				bos.write(out, 0, bytesread);
			}
			
			bis.close();
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}
}



class SoundPitch {
    static {
        System.loadLibrary("SoundPitch");
    }
	static native int initSound(int samplerate, int channels, int pitch);
	static native int processSound(byte[] in, byte[] out, int frames);
}

class SoundMix {
	static int mix(short[] in, short[] out, int samples) {
		int tmp;
		for (int i = 0; i < samples-1; i+=2) {
			out[i] = out[i+1] = (short)(((int)in[i] + (int)in[i+1])/2);
		}
		return 0;
	}
}
