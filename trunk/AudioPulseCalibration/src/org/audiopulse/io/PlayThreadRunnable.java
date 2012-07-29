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
	final static int channelPLAYConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
	final static int audioPLAYFormat = AudioFormat.ENCODING_PCM_16BIT;
	int streamMode= AudioManager.STREAM_MUSIC;
	int trackMode=AudioTrack.MODE_STREAM;
	final static int sampleRatePlay=8000;
	int Play_Buffer_Size;
	private short[] samples;
	Handler mainThreadHandler = null;
	int count = 0;
	private int IN_PLAY_MODE;
	public static String TAG = "PlayThreadRunnable";
	private long play_time;
	
	
	public PlayThreadRunnable(Handler h, double playTime)
	{
		Log.v(TAG,"constructing playback thread");
		mainThreadHandler = h;
		this.Play_Buffer_Size=(int) (playTime*sampleRatePlay);
		this.samples = new short[Play_Buffer_Size];
		this.initPlayTack();
		this.generateStimulus();
		this.IN_PLAY_MODE=0;
	}
	
	public synchronized void run()
	{
		Log.d(TAG,"Starting run in playback");
		informStart();
		//Send status of initialization
		if(this.mAudioPLAY.getState() != AudioTrack.STATE_INITIALIZED) {
			informMiddle("Error: Audio record was not properly initialized!!");
			return;
    	}
		
		//Play Stimulus
		this.IN_PLAY_MODE=1;
		informMiddle("Playing stimulus");
		playStimulus();
		this.IN_PLAY_MODE=0;
		//Finish up
		informFinish();
	}
	
	public void informMiddle(String str)
	{
		Log.v(TAG,"informing middle of playback");
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle(str));
		this.mainThreadHandler.sendMessage(m);
	}
	
	public int getPlayMode(){
		return this.IN_PLAY_MODE;
	}
	public void informStart()
	{
		Log.v(TAG,"informing start of playback");
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Starting playback"));
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		Log.v(TAG,"informing finish of playback");
		mAudioPLAY.release();
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Finished and released playback in " + play_time/1000 + " seconds"));
		this.mainThreadHandler.sendMessage(m);
	}
	
	
	 private void initPlayTack(){
		 Log.v(TAG,"Initializing playback track");
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
		 Log.v(TAG,"generating stimulus");
		 double[] f={2000,2400};
		 PeriodicSeries stimuli=new PeriodicSeries(this.samples.length,sampleRatePlay,f);
		 
		 this.samples=stimuli.generatePeriodicSeries();
		 
		 /*
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
		 */
	 }
	
	 
	 private void playStimulus() {
		Log.v(TAG,"playing stimulus");
		mAudioPLAY.play();
		long st = System.currentTimeMillis();
		mAudioPLAY.write( this.samples, 0, this.samples.length );
		play_time = System.currentTimeMillis()-st;
	 }
	 
}
