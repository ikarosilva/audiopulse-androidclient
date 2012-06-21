package AudioPulseCalibration.AudioPulse.org;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

public class RecordActivity extends Activity {
	protected static final String TAG = "RecordActivity";
	private int mAudioBufferSize;
	private int mAudioBufferSampleSize;
	private AudioRecord mAudioRecord;
	private boolean inRecordMode = false;
	final int sampleRate = 8000;
	final int stopTime = 10;
	final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	final int mAudioSource=MediaRecorder.AudioSource.MIC;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initAudioRecord();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "Resuming audio recording...");
		inRecordMode = true;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				getSamples();
			}
		});
		t.start();
	}

	protected void onPause() {
		Log.v(TAG, "Pausing audio recording...");
		inRecordMode = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "Destroying audio recording...");
		if(mAudioRecord != null) {
			mAudioRecord.release();
            Log.v(TAG, "Released AudioRecord");			
		}
		super.onDestroy();
	}
	
	public OnRecordPositionUpdateListener mListener = new OnRecordPositionUpdateListener(){
		
		public void onPeriodicNotification(AudioRecord recorder){
			Log.v(TAG,"in onPeriodicNotification");
		}
		public void onMarkerReached(AudioRecord recorder){
			Log.v(TAG," in onMarkerReached");
			inRecordMode = false;
		}
	};

	private void initAudioRecord() {
		try {
			mAudioBufferSize = 2 * AudioRecord.getMinBufferSize(sampleRate,
					channelConfig, audioFormat);
			mAudioBufferSampleSize = mAudioBufferSize / 2;
			mAudioRecord = new AudioRecord(
					mAudioSource,
					sampleRate,
					channelConfig,
					audioFormat,
					mAudioBufferSize);
			Log.v(TAG, "Setup of audio record succesfull. Buffer size = " + mAudioBufferSize);
			Log.v(TAG, "sampleRate = " + sampleRate);
			Log.v(TAG, "audioFormat= " + audioFormat);
			Log.v(TAG, "   Sample buffer size = " + mAudioBufferSampleSize);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		mAudioRecord.setNotificationMarkerPosition(stopTime);
		mAudioRecord.setRecordPositionUpdateListener(mListener);
		int audioRecordState = mAudioRecord.getState();
		if(audioRecordState != AudioRecord.STATE_INITIALIZED) {
			Log.e(TAG, "Audio record was not properly initialized!!");
			finish();
		}
		else {
			Log.v(TAG, "Audio record initialized");
		}
	}
		
	private void getSamples() {
		if(mAudioRecord == null) return;

		short[] audioBuffer = new short[mAudioBufferSampleSize];

		mAudioRecord.startRecording();

		int audioRecordingState = mAudioRecord.getRecordingState();
		if(audioRecordingState != AudioRecord.RECORDSTATE_RECORDING) {
			Log.e(TAG, "audio is not being recorded");
			finish();
		}
		else {
			Log.v(TAG, "starting recording of audio");
		}

		while(inRecordMode) {
		    int samplesRead = mAudioRecord.read(audioBuffer, 0, mAudioBufferSampleSize);
		    Log.v(TAG, "Got samples: " + samplesRead);
		}
		mAudioRecord.stop();
		Log.v(TAG, "AudioRecord has stopped recording");
		
		//Plot the collected data
		
	}
}