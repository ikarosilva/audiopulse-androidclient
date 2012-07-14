package org.audiopulse.calibration;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PlayThreadRunnable implements Runnable
{
	private int mPLAYAudioBufferSize;
	private AudioTrack mAudioPLAY;
	final static int channelPLAYConfig = AudioFormat.CHANNEL_OUT_MONO;
	final static int audioPLAYFormat = AudioFormat.ENCODING_PCM_16BIT;
	int streamMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	final static int sampleRatePlay=44100;
	final static int Play_Buffer_Size=sampleRatePlay*5;
	final short[] samples = new short[Play_Buffer_Size];
	Handler mainThreadHandler = null;
	int count = 0;
	public PlayThreadRunnable(Handler h)
	{
		mainThreadHandler = h;
	}
	public static String TAG = "PlayThreadRunnable";
	private long play_time;
	
	public void run()
	{
		Log.d(TAG,"Starting playback");
		informStart();
		initPlayTack();
		//Send status of initialization
		if(this.mAudioPLAY.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			return;
    	}
    	else {
    		informMiddle("Audio track sucessfully initialized.");
    	}
		
		//Fill Playback buffer
		generateStimulus();
		informMiddle("Generated stimulus");
		
		//Play Stimulus
		playStimulus();
		informMiddle("Playing stimulus");
		
		//Finish up
		informFinish();
	}
	
	public void informMiddle(String str)
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		this.mainThreadHandler.sendMessage(m);
	}
	
	public void informStart()
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Starting playback"));
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		mAudioPLAY.release();
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Finished and released playback in " + play_time/1000 + " seconds"));
		this.mainThreadHandler.sendMessage(m);
	}
	
	 private void initPlayTack(){
	    	try {
	    		mPLAYAudioBufferSize =AudioTrack.getMinBufferSize(sampleRatePlay, channelPLAYConfig, audioPLAYFormat);        
	    		mAudioPLAY = new AudioTrack(streamMode, sampleRatePlay, channelPLAYConfig, audioPLAYFormat, 
	    				mPLAYAudioBufferSize,trackMode);   
	    		mAudioPLAY.setStereoVolume(AudioTrack.getMaxVolume(),AudioTrack.getMaxVolume());
	    	} catch (IllegalArgumentException e) {
	    		e.printStackTrace();
	    	}	
	    }
	 
	
	 private void generateStimulus(){
		 float frequency1 = 2000;
		 float frequency2 = 2400;
		 final float increment1 = (float)(2*Math.PI) * frequency1 / sampleRatePlay; // angular increment for each sample
		 final float increment2 = (float)(2*Math.PI) * frequency2 / sampleRatePlay; // angular increment for each sample
		 float angle1 = 0;
		 float angle2 = 0;
		 for( int i = 0; i < this.samples.length; i++ )
		 {
			 this.samples[i] =(short) ((0.5*(float)Math.sin( angle1 ) + 0.60653066*0.5*(float)Math.sin( angle2 ))* Short.MAX_VALUE);
			 angle1 += increment1;
			 angle2 += increment2;
		 }
	 }
	
	 
	 private void playStimulus() {
		mAudioPLAY.play();
		long st = System.currentTimeMillis();
		mAudioPLAY.write( this.samples, 0, this.samples.length );
		play_time = System.currentTimeMillis()-st;
	 }
	 
}
