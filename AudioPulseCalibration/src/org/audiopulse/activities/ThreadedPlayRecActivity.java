package org.audiopulse.activities;
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
			this.playRecordThread();
			return true;
		}
		return true;
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
		if (playStatusBackHandler == null)
		{
			playStatusBackHandler = new ReportStatusHandler(this);
			recordStatusBackHandler = new ReportStatusHandler(this);
			playThread = 
					new Thread(
							new PlayThreadRunnable(playStatusBackHandler));
			recordThread = 
					new Thread(
							new RecordThreadRunnable(recordStatusBackHandler));

			playThread.setPriority(Thread.MAX_PRIORITY);
			recordThread.setPriority(Thread.MAX_PRIORITY);
			playThread.start();
			recordThread.start();
		}
		if (playThread.getState() != Thread.State.TERMINATED)
		{
			//Play thread is new or alive, but not terminated
		}
		else
		{
			//Play thread is likely dead, starting
			//Create a new thread, no way to resurrect a dead thread.
			playThread = 
					new Thread(
							new PlayThreadRunnable(playStatusBackHandler));
			playThread.start();
			recordThread = 
					new Thread(
							new RecordThreadRunnable(recordStatusBackHandler));
			recordThread.start();
		}
	}

	public void plotSamples(Bundle audioResultsBundle) {
		Log.v(TAG,"Starting activity for plotting results");
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		//Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}


}