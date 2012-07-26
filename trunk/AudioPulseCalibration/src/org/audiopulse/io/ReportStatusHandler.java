package org.audiopulse.io;

import org.audiopulse.activities.ThreadedPlayRecActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReportStatusHandler extends Handler
{
	public static final String TAG = "ReportStatusHandler";
	private ThreadedPlayRecActivity parentActivity = null; 
	
	public ReportStatusHandler(ThreadedPlayRecActivity inParentActivity)
	{
		//Registering handler in parent activity 
		parentActivity = inParentActivity;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		Log.v(TAG,"Handling received message");
		String pm = Utils.getStringFromABundle(msg.getData());		
		Bundle b=msg.getData();
		if(b.getLong("N") == 0L){
			Log.v(TAG,"Extracting further data");
			this.printMessage(pm);
		}else{
			this.plotData(b);
		}
				
		
	}

	private void printMessage(String str)
	{
		//Printing status
		Log.v(TAG,"printing message");
		parentActivity.appendText(str);
	}
	
	private void plotData(Bundle b)
	{
		//Printing status
		Log.v(TAG,"Plotting data from bundle");
		parentActivity.audioResultsBundle=b;
		parentActivity.plotSamples();
	}
}
