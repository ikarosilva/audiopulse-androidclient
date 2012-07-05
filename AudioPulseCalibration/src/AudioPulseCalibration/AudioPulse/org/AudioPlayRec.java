package AudioPulseCalibration.AudioPulse.org;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AudioPlayRec extends Activity implements Runnable {

		private TextView statusText;
		private static String TAG="AudioPlayRec";
		private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		int frequency = 8000;
		int bufferSize = 2*AudioTrack.getMinBufferSize(frequency,
				AudioFormat.CHANNEL_OUT_MONO,audioEncoding);
		short[] buffer = new short[bufferSize];
		String text_string;
		final Handler mHandler = new Handler();
		
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.audiorecplay);
			statusText= (TextView) findViewById(R.id.status);
			Button actionButton = (Button) findViewById(R.id.record);
			actionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					record_thread();
				}
			});
		}
		
		final Runnable mUpdateResults = new Runnable(){
			public void run(){
				updateResultsInUi(text_string);
			}
		};
			
		private void updateResultsInUi(String update_txt){
			statusText.setText(update_txt);
		}
		
		private void record_thread(){
			Thread thread = new Thread(new Runnable (){
				public void run(){
					text_string ="Starting";
					mHandler.post(mUpdateResults);
					Log.v(TAG,"Recording audio");
					record();
					text_string="Done";
					mHandler.post(mUpdateResults);
				}
			});
			thread.start();
		}
		
		
		int mAudioBufferSize = AudioRecord.getMinBufferSize( 8000,AudioFormat.CHANNEL_IN_MONO,audioEncoding)/2;
		short[] buffer2 = new short[mAudioBufferSize];
		public AudioRecord audioRecord = new AudioRecord(
				MediaRecorder.AudioSource.MIC,
				8000, AudioFormat.CHANNEL_IN_MONO,
				audioEncoding, mAudioBufferSize);
		public AudioTrack audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, frequency,
				AudioFormat.CHANNEL_OUT_MONO,
				audioEncoding, 4096,
				AudioTrack.MODE_STREAM);
		
		
		public void record(){
			try{
				audioRecord.startRecording();
				audioRecord.read(buffer2,0,mAudioBufferSize);
				audioRecord.stop();
				for (int i=0; i < mAudioBufferSize; i++){
					Log.v(TAG,"buffer2[" +i + "]= " + buffer2[i]);
				}
			}catch (Throwable t){
				Log.e(TAG,"Audio Recording failed");
			}
		}
		
		public void run(){
			audioTrack.setStereoVolume(AudioTrack.getMaxVolume(),AudioTrack.getMaxVolume());
			audioTrack.play();
			//should also work:
			audioTrack.write(buffer,0,bufferSize);
			//int n=0;
			//while(n<bufferSize){
			//	audioTrack.write(buffer,n++,1);
			//}
			return;
		}
		
		@Override
		protected void onPause(){
			if(audioTrack!=null){
				if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
					audioTrack.pause();
				}
			}
			super.onPause();
		}
	
}