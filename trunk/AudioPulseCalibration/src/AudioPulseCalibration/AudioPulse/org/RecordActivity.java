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
 * RecordActivity.java
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

package AudioPulseCalibration.AudioPulse.org;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

public class RecordActivity extends Activity {
	protected static final String TAG = "RecordActivity";
	private int mRECAudioBufferSize;
	private int mRECAudioBuffer;
	private AudioRecord mAudioREC;
	private boolean inRecordMode = false;
	final int sampleRateREC = 8000;
	final int stopTimeREC = 2; //in frames
	final int channelRECConfig = AudioFormat.CHANNEL_IN_MONO;
	final int audioRECFormat = AudioFormat.ENCODING_PCM_16BIT;
	final int mAudioRECSource=MediaRecorder.AudioSource.MIC;
	private int mPLAYAudioBufferSize;
	private AudioTrack mAudioPLAY;
	final int sampleRatePLAY = sampleRateREC; //For now keep the same, but allow it to vary in the future...
	final int channelPLAYConfig = AudioFormat.CHANNEL_IN_MONO;
	final int audioPLAYFormat = AudioFormat.ENCODING_PCM_16BIT;
	final int mAudioPLAYSource=MediaRecorder.AudioSource.MIC;
	int modePLAY= AudioTrack.MODE_STREAM;
	final int Play_Buffer_Size=sampleRatePLAY*2;
	final float frequency1 = 2000;
    final float frequency2 = 2400;
    final float samples[] = new float[sampleRatePLAY];
    Bundle audio_bundle = new Bundle();
    short[] audioBuffer = new short[mRECAudioBuffer];
    int nFFT=1024;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getStimulus();
		initAudioRecord();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "Resuming audio recording...");
		inRecordMode = true;
		Thread play_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				playSamples();
			}
		});
		
		Thread record_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				getSamples();
			}
		});
		play_thread.setPriority(Thread.MAX_PRIORITY);
		play_thread.start();
		record_thread.setPriority(Thread.MAX_PRIORITY);
		record_thread.start();
		Log.v(TAG, "Waiting for thread to finish and then plot results...");
		plotSamples();
	}

	protected void onPause() {
		Log.v(TAG, "Pausing audio recording...");
		inRecordMode = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "Destroying audio recording...");
		if(mAudioREC != null) {
			mAudioREC.release();
            Log.v(TAG, "Released recording");			
		}
		if(mAudioPLAY != null) {
			mAudioPLAY.release();
            Log.v(TAG, "Released playback");			
		}
		super.onDestroy();
	}
	
	public OnRecordPositionUpdateListener mListener = new OnRecordPositionUpdateListener(){
		
		public void onPeriodicNotification(AudioRecord recorder){
			Log.v(TAG,"in onPeriodicNotification");
		}
		public void onMarkerReached(AudioRecord recorder){
			inRecordMode = false;
		}
	};

	private void initAudioRecord() {
		try {
			mRECAudioBufferSize = 5 * AudioRecord.getMinBufferSize(sampleRateREC,
					channelRECConfig, audioRECFormat);
			mRECAudioBuffer = mRECAudioBufferSize / 2;
			mAudioREC = new AudioRecord(
					mAudioRECSource,
					sampleRateREC,
					channelRECConfig,
					audioRECFormat,
					mRECAudioBufferSize);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		mAudioREC.setNotificationMarkerPosition(stopTimeREC);
		mAudioREC.setRecordPositionUpdateListener(mListener);
		int audioRecordState = mAudioREC.getState();
		if(audioRecordState != AudioRecord.STATE_INITIALIZED) {
			Log.e(TAG, "Audio record was not properly initialized!!");
			finish();
		}
		else {
			Log.v(TAG, "Audio record initialized");
		}
			
		try {
			mPLAYAudioBufferSize =AudioTrack.getMinBufferSize( sampleRatePLAY, channelPLAYConfig, audioPLAYFormat);        
			mAudioPLAY = new AudioTrack( mAudioPLAYSource, sampleRatePLAY, 
					channelPLAYConfig, audioPLAYFormat, 
					mPLAYAudioBufferSize, modePLAY);   
		      mAudioPLAY.setStereoVolume(AudioTrack.getMaxVolume(),AudioTrack.getMaxVolume());
		      Log.v(TAG, "Initialized Playing Track");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		if(audioRecordState != AudioRecord.STATE_INITIALIZED) {
			Log.e(TAG, "Audio record was not properly initialized!!");
			finish();
		}
		else {
			Log.v(TAG, "Audio track initialized");
		}
	}
			
	private void getStimulus(){
		final float increment1 = (float)(2*Math.PI) * frequency1 / sampleRatePLAY; // angular increment for each sample
	    final float increment2 = (float)(2*Math.PI) * frequency2 / sampleRatePLAY; // angular increment for each sample
	    float angle1 = 0;
	    float angle2 = 0;
		for( int i = 0; i < samples.length; i++ )
        {
           samples[i] = (float) (0.5*(float)Math.sin( angle1 ) + 0.60653066*0.5*(float)Math.sin( angle2 ));
           angle1 += increment1;
           angle2 += increment2;
        }
	}
	
	private void playSamples() {
		//Start playing calibration sound before recording
        Thread play_thread = new Thread( new Runnable( ) 
	      {
	         public void run()
	         {        		
	            AudioPlay device = new AudioPlay(sampleRatePLAY);
	               device.writeSamples( samples );
	         }
	      } );
        play_thread.setPriority(Thread.MAX_PRIORITY);
        play_thread.start();
		}
	
	private void getSamples() {
		if(mAudioREC == null) return;	
		//Start playing calibration sound before recording
        Thread play_thread = new Thread( new Runnable( ) 
	      {
	         public void run()
	         {        		
	            AudioPlay device = new AudioPlay(sampleRatePLAY);
	               device.writeSamples( samples );
	         }
	      } );
	      
        
        play_thread.setPriority(Thread.MAX_PRIORITY);
        play_thread.start();
		
		mAudioREC.startRecording();
		int audioRecordingState = mAudioREC.getRecordingState();
		if(audioRecordingState != AudioRecord.RECORDSTATE_RECORDING) {
			Log.e(TAG, "audio is not being recorded");
			finish();
		}
		else {
			Log.v(TAG, "starting recording of audio");
		}
		while(inRecordMode) {
		    int samplesRead = mAudioREC.read(audioBuffer, 0, mRECAudioBuffer);
		    Log.v(TAG, "Got samples: " + samplesRead);
		}	
		mAudioREC.stop();
		Log.v(TAG, "Recording has stopped recording, plotting data");
	}
	
	private void plotSamples() {
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		//Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		audio_bundle.putShortArray("audio_data",this.audioBuffer);
		audio_bundle.putInt("sampleRateREC",sampleRateREC);
		audio_bundle.putInt("N",nFFT);
		intent.putExtras(audio_bundle);
        startActivity(intent);
	}
	
}