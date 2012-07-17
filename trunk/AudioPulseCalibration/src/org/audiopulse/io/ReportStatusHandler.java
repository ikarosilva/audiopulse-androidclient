package org.audiopulse.io;

import org.audiopulse.activities.ThreadedPlayRecActivity;

import android.os.Handler;
import android.os.Message;

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
		String pm = Utils.getStringFromABundle(msg.getData());		
		this.printMessage(pm);
	}

	private void printMessage(String str)
	{
		//Printing status
		parentActivity.appendText(str);
	}
}
