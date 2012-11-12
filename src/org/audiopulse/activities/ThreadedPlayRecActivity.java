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

package org.audiopulse.activities;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.audiopulse.R;
import org.audiopulse.io.PlayThreadRunnable;
import org.audiopulse.io.RecordThreadRunnable;
import org.audiopulse.io.ReportStatusHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ThreadedPlayRecActivity extends AudioPulseRootActivity 
{
	public static final String TAG="ThreadedPlayRecActivity";
	
	static final int STIMULUS_DIALOG_ID = 0;
	Bundle audio_bundle = new Bundle();
	Handler playStatusBackHandler = null;
	Handler recordStatusBackHandler = null;
	Thread playThread = null;
	Thread recordThread = null;
	public static double playTime=0.5;
	public Bundle audioResultsBundle;
	ScheduledThreadPoolExecutor threadPool=new ScheduledThreadPoolExecutor(2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
		
		// set listener for main menu items
		ListView menuList = (ListView) findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(
        	new AdapterView.OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        			
        			TextView item = (TextView) itemClicked;
        			String itemText = item.getText().toString();
        			
        			if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_plot))) {
        				plotWaveform();
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_all_right))) {
        				emptyText(); //Clear text for new Test
        				plotAudiogram();	
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_all_left))) {
        				emptyText(); //Clear text for new Test
        				plotAudiogram();
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_4k))) {
        				emptyText(); //Clear text for new Test
        				playRecordThread(getResources().getString(R.string.menu_4k));
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_3k))) {
        				emptyText(); //Clear text for new Test
        				playRecordThread(getResources().getString(R.string.menu_3k));
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_2k))) {
        				emptyText(); //Clear text for new Test
        				playRecordThread(getResources().getString(R.string.menu_2k));
        			} else if (itemText.equalsIgnoreCase(getResources().getString(R.string.menu_spontaneous))) {
        				emptyText(); //Clear text for new Test
        				playRecordThread(getResources().getString(R.string.menu_spontaneous));
        			} 
        		}
        	}
		);
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
		int selected_id=item.getItemId();
		if (selected_id == R.id.menu_clear)
		{
			//TODO: Item id menu_clear not being used anymore. Consider removing in future versions
			emptyText();
			return true;
		}
		if (selected_id == R.id.menu_3k || selected_id == R.id.menu_2k ||
			selected_id == R.id.menu_4k || selected_id == R.id.menu_spontaneous)
		{
			playRecordThread("spontaneous");
			return true;
		}
		if(selected_id == R.id.menu_all_right || selected_id == R.id.menu_all_left )
		{
			plotAudiogram();
			return true;
		}
		if(selected_id == R.id.plot_waveform)
		{
			plotWaveform();
			return true;
		}
		if(selected_id == R.id.menu_stimulus) {
			editStimulusSettings();
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

	private void playRecordThread(String menu_selected)
	{
		
		//Ignore playing thread when obtaining SOAEs
		
		beginTest();	
		Context context=this.getApplicationContext();		
		
		
		recordStatusBackHandler = new ReportStatusHandler(this);
		RecordThreadRunnable rRun = new RecordThreadRunnable(recordStatusBackHandler,playTime,context);
		
		if(menu_selected.equalsIgnoreCase(getResources().getString(R.string.menu_spontaneous)) ){
			ExecutorService execSvc = Executors.newFixedThreadPool( 1 );
			rRun.setExpectedFrequency(0);
			recordThread = new Thread(rRun);	
			recordThread.setPriority(Thread.MAX_PRIORITY);
			execSvc.execute( recordThread );
			execSvc.shutdown();
		}else{
			playStatusBackHandler = new ReportStatusHandler(this);
			PlayThreadRunnable pRun = new PlayThreadRunnable(playStatusBackHandler,playTime);
			ExecutorService execSvc = Executors.newFixedThreadPool( 2 );
			playThread = new Thread(pRun);
			rRun.setExpectedFrequency(pRun.getExpectedFrequency());
			recordThread = new Thread(rRun);	
			playThread.setPriority(Thread.MAX_PRIORITY);
			recordThread.setPriority(Thread.MAX_PRIORITY);
			execSvc.execute( recordThread );
			execSvc.execute( playThread );
			execSvc.shutdown();
		}
		endTest();
	}

	public void plotSpectrum() {
		Intent intent = new Intent(this.getApplicationContext(), PlotSpectralActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}

	public void plotWaveform() {
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}
	
	public void plotAudiogram() {
		Intent intent = new Intent(this.getApplicationContext(), PlotAudiogramActivity.class);
		startActivity(intent);
	}
	
	public void editStimulusSettings() {
		//TODO Change this so that the menu has a limited set of stimulus avaiable from
		//which the clinician can operate the app
		Log.v(TAG,"Calling view to edit stimulus settings");
		Intent intent = new Intent(this.getApplicationContext(), PlotWaveformActivity.class);
		intent.putExtras(audioResultsBundle);
		startActivity(intent);
	}
	

}