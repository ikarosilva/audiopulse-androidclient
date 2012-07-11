package org.audiopulse.calibration;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PlayThreadRunnable implements Runnable
{
	Handler mainThreadHandler = null;
	int count = 0;
	public PlayThreadRunnable(Handler h)
	{
		mainThreadHandler = h;
	}
	public static String TAG = "PlayThreadRunnable";
	public void run()
	{
		Log.d(TAG,"start playing audio");
		informStart();
		for(int i=1;i <= 10;i++)
		{
			Utils.sleepForInSecs(1);
			informMiddle(i);
		}
		informFinish();
	}
	
	public void informMiddle(int count)
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Playing Audio"));
		this.mainThreadHandler.sendMessage(m);
	}
	
	public void informStart()
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Starting playback"));
		this.mainThreadHandler.sendMessage(m);
	}
	public void informFinish()
	{
		Message m = this.mainThreadHandler.obtainMessage();
		m.setData(Utils.getStringAsABundle("Finishing playback"));
		this.mainThreadHandler.sendMessage(m);
	}
}
