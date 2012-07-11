package org.audiopulse.calibration;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReportStatusHandler extends Handler
{
	public static final String TAG = "ReportStatusHandler";
	private ThreadedPlayRecActivity parentActivity = null; 
	
	public ReportStatusHandler(ThreadedPlayRecActivity inParentActivity)
	{
		parentActivity = inParentActivity;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		String pm = Utils.getStringFromABundle(msg.getData());
				
		Log.d(TAG,pm);
		this.printMessage(pm);
	}

	private void printMessage(String str)
	{
		parentActivity.appendText(str);
	}
}
