package org.audiopulse.calibration;
import android.app.Activity;
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
    		this.playThread();
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
   
    Handler statusBackHandler = null;
    Thread workerThread = null;
    private void playThread()
    {
    	if (statusBackHandler == null)
    	{
    		statusBackHandler = new ReportStatusHandler(this);
        	workerThread = 
        		new Thread(
        				new PlayThreadRunnable(statusBackHandler));
        	workerThread.start();
    	}
    	if (workerThread.getState() != Thread.State.TERMINATED)
    	{
    		Log.d(TAG, "Play thread is new or alive, but not terminated");
    	}
    	else
    	{
    		Log.d(TAG, "Play thread is likely dead, starting");
    		//Create a new thread, no way to resurrect a dead thread.
        	workerThread = 
        		new Thread(
        				new PlayThreadRunnable(statusBackHandler));
    		workerThread.start();
    	}
    }
}