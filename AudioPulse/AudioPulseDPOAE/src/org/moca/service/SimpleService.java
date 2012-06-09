package org.moca.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Process;

import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.app.Service;


public class SimpleService extends Service {

	static File root = Environment.getExternalStorageDirectory();
	//
		
	@Override
	public IBinder onBind(Intent arg0){
		return null;
	}

	boolean paused =false;

	@Override
	public void onCreate(){
		super.onCreate();
		paused=false;
		Thread initBkgdThread = new Thread( new Runnable() {
			public void run(){
				write_file();
			}
		});
		initBkgdThread.start();
	}

	@Override public void onDestroy() {
		super.onDestroy();
		Process.killProcess(Process.myPid());
		paused= true;
		
	}

	private void write_file(){
		if (root.canWrite()){
			File gpxfile = new File(root, "test.txt");
			FileWriter gpxwriter = null;
			try {
				gpxwriter = new FileWriter(gpxfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter out = new BufferedWriter(gpxwriter);
			short [] sig= new short[8];
			float twopi = (float)Math.PI *2f;
			float iSignalMax = (16384/100);
			try {
				for(int i=0;i< 15*500;i++){
					//sig[0]= (short) (iSignalMax * Math.sin(twopi*i*(2/500f)));
					sig[0]= (short)( i);
					sig[1]= (short) (sig[0]+ (short) (iSignalMax * Math.sin(twopi*i*(4/500f))));
					sig[2]= (short) (sig[1]- (short) (iSignalMax * Math.sin(twopi*i*(6/500f))));
					sig[3]= (short) (sig[2]+ (short) (iSignalMax * Math.sin(twopi*i*(8/500f))));
					sig[4]= (short) (sig[3]- (short) (iSignalMax * Math.sin(twopi*i*(10/500f))));
					sig[5]= (short) (sig[4]+ (short) (iSignalMax * Math.sin(twopi*i*(12/500f))));
					//sig[6]= (short) (sig[5]- (short) (iSignalMax * Math.sin(twopi*i*(14/500f))));
					sig[7]= (short)( i);
					sig[6]= (short)( i);
					out.write(Short.toString(sig[0]) + "," +Short.toString(sig[1]) + "," +Short.toString(sig[2]) + "," +Short.toString(sig[3]) + "," 
							+ Short.toString(sig[4]) + "," + Short.toString(sig[5]) + "," + Short.toString(sig[6]) + "," +Short.toString(sig[7]) + "," 
							+"\n" );
					Thread.sleep(0);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}