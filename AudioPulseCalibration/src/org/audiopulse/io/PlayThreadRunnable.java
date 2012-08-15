/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.io;

import org.audiopulse.utilities.CalibrationTone;
import org.audiopulse.utilities.PeriodicSeries;
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
	int channelPLAYConfig = AudioFormat.CHANNEL_OUT_STEREO;
	int audioPLAYFormat = AudioFormat.ENCODING_PCM_16BIT;
	int streamMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	final static int sampleRatePlay=8000;
	int PlayBufferSize;
	private short[] samples;
	Handler mainThreadHandler = null;
	public static String TAG = "PlayThreadRunnable";
	private long play_time;


	public PlayThreadRunnable(Handler h, double playTime)
	{
		Log.v(TAG,"constructing playback thread");
		mainThreadHandler = h;
		PlayBufferSize=(int) (playTime*sampleRatePlay);

		if(channelPLAYConfig == AudioFormat.CHANNEL_OUT_STEREO){
			//Interleave the tracks for processing
			samples = new short[2*PlayBufferSize];
		}else{
			samples = new short[PlayBufferSize];
		}
		this.initPlayTrack();
		this.generateStimulus();

	}

	public synchronized void run()
	{
		Log.d(TAG,"Starting run in playback");
		informStart();
		//Send status of initialization
		if(mAudioPLAY.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			return;
		}

		//Play Stimulus
		informMiddle("Playing stimulus");
		playStimulus();
		//Finish up
		informFinish();
	}

	public void informMiddle(String str)
	{
		Log.v(TAG,"informing middle of playback");
		Message m = mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		mainThreadHandler.sendMessage(m);
	}

	public void informStart()
	{
		Log.v(TAG,"informing start of playback");
		Message m = mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Starting playback"));
		mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		Log.v(TAG,"informing finish of playback");
		mAudioPLAY.release();
		Message m = mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Finished and released playback in " + play_time/1000 + " seconds"));
		mainThreadHandler.sendMessage(m);
	}


	private void initPlayTrack(){
		Log.v(TAG,"Initializing playback track");
		try {
			mPLAYAudioBufferSize =AudioTrack.getMinBufferSize(sampleRatePlay, channelPLAYConfig, audioPLAYFormat)*2;   
			Log.v(TAG,"mPLAYAudioBufferSize ="+mPLAYAudioBufferSize);
			mAudioPLAY = new AudioTrack(streamMode, sampleRatePlay, channelPLAYConfig, audioPLAYFormat, 
					mPLAYAudioBufferSize,trackMode);   
			mAudioPLAY.setStereoVolume(AudioTrack.getMaxVolume(),AudioTrack.getMaxVolume());
			mAudioPLAY.setPlaybackRate(sampleRatePlay);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}	
		Log.v(TAG,"Channel count is:" + mAudioPLAY.getChannelCount());
		Log.v(TAG,"Fs count is:" + mAudioPLAY.getPlaybackRate());
		Log.v(TAG,"Coding:" + mAudioPLAY.getAudioFormat());
		Log.v(TAG,"Fs count is:" + mAudioPLAY.getSampleRate());
		
		if(mAudioPLAY.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			Log.e(TAG,"Error: Audio record was not properly initialized!!");
		}
	}


	private void generateStimulus(){
		Log.v(TAG,"generating stimulus of length = " + (long) PlayBufferSize/PlayThreadRunnable.sampleRatePlay + " seconds");
		CalibrationTone caltone = new CalibrationTone(CalibrationTone.device.ER10C);
		PeriodicSeries stimuli=new PeriodicSeries(PlayBufferSize, PlayThreadRunnable.sampleRatePlay,caltone);
		short[] tmpSamples = stimuli.generatePeriodicSeries();
		if(channelPLAYConfig == AudioFormat.CHANNEL_OUT_STEREO){
			//Interleave the tracks for processing
			for(int i=0;i<tmpSamples.length;i++){
				if(i<(samples.length/2)){
					samples[i]=tmpSamples[i];
					samples[i+1]=tmpSamples[i];
				}else{
					samples[i]=0;
					samples[i+1]=0;
				}

			}
		}else {
			//Mono channel
			samples=tmpSamples;
		}
	}


	private void playStimulus() {
		int frameSize=mPLAYAudioBufferSize; 
		int dataLeft=samples.length;	
		int ind=0;
		int endbuffer;
		int nWrite=1;
		int total=0;
		Log.v(TAG,"frame size is: " + frameSize + " card size is: " + mPLAYAudioBufferSize+ " play time is: " + PlayBufferSize);
		Log.v(TAG, "dataleft to play: " + dataLeft);
		mAudioPLAY.play();
		long st = System.currentTimeMillis();
		while(dataLeft>0){
			endbuffer=(frameSize<dataLeft) ? frameSize: dataLeft;	
			Log.v(TAG, "Index: " + ind*frameSize + " size: " + endbuffer);
			nWrite=mAudioPLAY.write( samples,ind*frameSize,endbuffer);
			if (nWrite == AudioTrack.ERROR_INVALID_OPERATION || nWrite == AudioTrack.ERROR_BAD_VALUE) {
				Log.e(TAG, "Audio read failed: " + nWrite);
				break;
			}
			Log.v(TAG,"wrote " + nWrite + " samples ( " + (long) 100*nWrite/endbuffer + " %)");
			dataLeft -= endbuffer;
			Log.v(TAG, "dataleft to play: " + dataLeft);
			ind++;	
			total+=nWrite;
		}
		play_time = System.currentTimeMillis()-st;
		Log.v(TAG,"play time " + play_time/1000 + " seconds" + " total samples: " + total);
	}

}












