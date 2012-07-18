package org.audiopulse.io;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RecordThreadRunnable implements Runnable
{
	private int mAudioBufferSize;
	private AudioRecord mAudio;
	final static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	final static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int streamMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	final static int sampleRate=44100;
	final static int Buffer_Size=sampleRate*5;
	final short[] samples = new short[Buffer_Size];
	Handler mainThreadHandler = null;
	
	public RecordThreadRunnable(Handler h)
	{
		mainThreadHandler = h;
	}
	public static String TAG = "RecordThreadRunnable";
	private long record_time;
	
	public void run()
	{
		Log.d(TAG,"Starting recording");
		informStart();
		initRecord();
		//Send status of initialization
		if(this.mAudio.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			return;
    	}
    	else {
    		informMiddle("Audio track sucessfully initialized.");
    	}
		
		//Record Stimulus
		record();
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
		m.setData(Utils.getStringAsABundle("Starting recording"));
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		mAudio.release();
		Message m = this.mainThreadHandler.obtainMessage();
		Bundle results= new Bundle();
		String msg="Finished and released recording in " + record_time/1000 + " seconds";
		results.putCharArray("message", msg.toCharArray());
		results.putShortArray("samples",this.samples);
		results.putFloat("recSampleRate",this.sampleRate);
		results.putLong("N",this.samples.length);
		m.setData(results);
		this.mainThreadHandler.sendMessage(m);
		
	}
	
	 private void initRecord(){
	    	try {
	    		mAudioBufferSize =AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat)*2;         
	    		mAudio = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,
                        audioFormat, mAudioBufferSize);
	    	} catch (IllegalArgumentException e) {
	    		e.printStackTrace();
	    	}	
	    }
	 
	
	  
	 private void record() {
		mAudio.startRecording();
		long st = System.currentTimeMillis();
        mAudio.read(this.samples,0,this.samples.length);
        mAudio.stop();
		record_time = System.currentTimeMillis()-st;
	 }
	 
}
