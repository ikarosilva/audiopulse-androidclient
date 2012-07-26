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
	private static final String TAG="RecordThreadRunnable";
	private double record_time;
	private double playTime;
	private int soundCardBufferSize;
	private AudioRecord mAudio;
	final static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	final static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int streamMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	private int sampleRate=44100;
	private int Buffer_Size;
	private int soundCardChunkSize=160;
	private int IN_REC_MODE;
	final short[] samples ;
	Handler mainThreadHandler = null;
	private Bundle results;
	private static final int sizeOfShort=2; //Size of short in Bytes

	public RecordThreadRunnable(Handler h, double playTime)
	{
		Log.v(TAG,"constructing record thread");
		mainThreadHandler = h;
		this.Buffer_Size=(int) (playTime*sampleRate);
		this.samples = new short[Buffer_Size];
		this.initRecord();
		this.playTime=playTime;
		this.IN_REC_MODE=0;
	}

	public synchronized void run()
	{
		Log.d(TAG,"Starting run in recording");
		informStart();

		//Send status of initialization
		if(this.mAudio.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			return;
		}

		//Record Stimulus
		informMiddle("Recording stimulus");
		Log.d(TAG,"Recording stimulus");
		this.IN_REC_MODE=1;
		record();
		this.IN_REC_MODE=0;

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
		Log.v(TAG,"inform record starting");
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Starting recording"));
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		Log.v(TAG,"Informing recording finish");
		mAudio.release();
		Message m = this.mainThreadHandler.obtainMessage();
		this.results= new Bundle();
		String msg="Finished and released recording in " + this.record_time/1000 + " seconds";
		this.results.putString("message", msg);
		this.results.putShortArray("samples",this.samples);
		this.results.putFloat("recSampleRate",this.sampleRate);
		this.results.putLong("N",(long) this.samples.length);
		m.setData(this.results);
		this.mainThreadHandler.sendMessage(m);
	}

	public int getRecMode(){
		return this.IN_REC_MODE;
	}

	private void initRecord(){
		Log.v(TAG,"Initialized record track");
		try {
			int tmpSize=AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);         
			soundCardBufferSize=Math.max(this.samples.length*sizeOfShort,tmpSize);
			mAudio = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,
					audioFormat, soundCardBufferSize);
			mAudio.setPositionNotificationPeriod(soundCardChunkSize);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	

	}



	private void record() {
		Log.v(TAG,"Starting recording of "+ this.samples.length +" samples through mAudio");
		long st = System.currentTimeMillis();
		mAudio.startRecording();
		while(System.currentTimeMillis()-st < (playTime*1000)){
			mAudio.read(this.samples,0,this.samples.length);
		}
		mAudio.stop();
		record_time = System.currentTimeMillis()-st;
		Log.v(TAG,"low level recording took: " + record_time/1000);
	}

}
