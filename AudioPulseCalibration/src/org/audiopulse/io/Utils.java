package org.audiopulse.io;

import java.util.Iterator;
import android.os.Bundle;
import android.util.Log;

public class Utils 
{
	private static final String TAG="Utils";
	protected static Bundle b;
	
	public static long getThreadId()
	{
		Thread t = Thread.currentThread();
		return t.getId();
	}

	public static String getThreadSignature()
	{
		Thread t = Thread.currentThread();
		long l = t.getId();
		String name = t.getName();
		long p = t.getPriority();
		String gname = t.getThreadGroup().getName();
		return (name + ":(id)" + l + ":(priority)" + p
				+ ":(group)" + gname);
	}
	
	public static void logThreadSignature()
	{
		Log.d(TAG, getThreadSignature());
	}
	
	public static Bundle getStringAsABundle(String message)
	{
		Log.v(TAG,"Getting string AS A Bundle");
		b= new Bundle();
		b.putString("message", message);
		return b;
	}
	public static String getStringFromABundle(Bundle b)
	{
		Log.v(TAG,"Getting string FROM A Bundle");
		return b.getString("message");
	}
}
