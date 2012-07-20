package org.audiopulse.io;

import java.util.Iterator;
import java.util.Set;

import android.os.Bundle;
import android.util.Log;

public class Utils 
{
	private static final String TAG="Utils";
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
		Log.v(TAG,"Getting string as bundle");
		Bundle b = new Bundle();
		b.putString("message", message);
		return b;
	}
	public static String getStringFromABundle(Bundle b)
	{
		Log.v(TAG,"Getting string from Bundle");
		Log.v(TAG,"Printing bundle keys...:");
		for(Iterator<String> k = b.keySet().iterator(); k.hasNext();){
			Log.v(TAG,k.next().toString());
		}
			
		String str= b.getString("message");
		Log.v(TAG,str);
		return b.getString("message");
	}
}
