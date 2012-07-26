package org.audiopulse.activities;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ThreadedPlayRecActivity extends Activity 
{
	public static final String TAG="ThreadedPlayRecActivity";
	Bundle audio_bundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=1;
	public Bundle audioResultsBundle;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater(); //from activity
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		appendMenuItemText(item);
		if (item.getItemId() == R.id.menu_clear)
		{
			this.emptyText();
			return true;
		}
		if (item.getItemId() == R.id.menu_play_thread)
		{
			Log.v(TAG,"Starting execution of thread pool");
			this.playRecordThread();
			return true;
		}
		if(item.getItemId() == R.id.plot_waveform){
			this.plotWaveform();
			return true;
		}
		return false;
	}

	private TextView getTextView(){
		return (TextView)this.findViewById(R.id.text1);
	}
	public void appendText(String str){
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + str);
	}
	private void appendMenuItemText(MenuItem menuItem){
		String title = menuItem.getTitle().toString();
		TextView tv = getTextView(); 
		tv.setText(tv.getText() + "\n" + title);
	}
	private void emptyText(){
		TextView tv = getTextView();
		tv.setText("");
	}

	private void playRecordThread()
	{
		playStatusBackHandler = new ReportStatusHandler(this);
		recordStatusBackHandler = new ReportStatusHandler(this);
		ExecutorService execSvc = Executors.newFixedThreadPool( 2 );
		playThread = 
				new Thread(
						new PlayThreadRunnable(playStatusBackHandler,playTime));
		recordThread = 
				new Thread(
						new RecordThreadRunnable(recordStatusBackHandler,playTime));

		//playThread.setPriority(Thread.MAX_PRIORITY);
		//recordThread.setPriority(Thread.MAX_PRIORITY);
		
		
		Log.v(TAG,"Executing thread pool");
		long st=System.currentTimeMillis();
		//playThread.start();
		//recordThread.start();
		execSvc.execute( playThread );
		execSvc.execute( recordThread );
		execSvc.shutdown();
		
		/*threadPool.execute(playThread);
		threadPool.execute(recordThread);
		threadPool.shutdown();
		*/
		long nt=System.currentTimeMillis();
		Log.v(TAG,"shutting down thread pool after: " + (st-nt)/1000 );
	}

	public void plotSamples() {
		Log.v(TAG,"Sample rate is " + this.audioResultsBundle.getFloat("recSampleRate"));
		Log.v(TAG,"Calling view to plot data");
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		intent.putExtras(this.audioResultsBundle);
		startActivity(intent);
	}
	
	public void plotWaveform() {
		Log.v(TAG,"Calling view to plot waveform data");
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(this.audioResultsBundle);
		startActivity(intent);
	}


}