package org.audiopulse.tests;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.audiopulse.activities.TestActivity;
import org.audiopulse.analysis.AudioPulseDataAnalyzer;
import org.audiopulse.analysis.TEOAEKempAnalyzer;
import org.audiopulse.io.AudioPulseFileWriter;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.Signals;

import android.os.Bundle;
import android.util.Log;

public class TEOAEProcedure extends TestProcedure{

	private final String TAG = "TEOAEProcedure";
	private Bundle data;
	//TODO: Select appropiate stimulus duration
	private final double stimulusDuration=20;//stimulus duration in seconds
	private short[] results;
	private HashMap<String, Double> DPGRAM;
	private HashSet<String> fileNames=new HashSet<String>();
	private HashMap<String,String> fileNamestoDataMap=new HashMap<String,String>();
	
	public TEOAEProcedure(TestActivity parentActivity) {
		super(parentActivity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		
		clearLog();
		sendMessage(TestActivity.Messages.PROGRESS);
		logToUI("Running TEOAE");
		//create {f1, f2} tones in {left, right} channel of stereo stimulus
		double[] probe = Signals.clickKempMethod(super.playbackSampleFrequency, 
				stimulusDuration);
		probe = hardware.setOutputLevel(probe, 35);
		short[] interLeavedProbe=
				Signals.interleave(AudioSignal.convertMonoToShort(probe));
		//TODO: Fix issues with the signal being clipped/buffer overuns ?
		//setPlaybackAndRecording ASSUMES STEREO INTERLEAVED!
		testIO.setPlaybackAndRecording(interLeavedProbe);
		double stTime= System.currentTimeMillis();
		results = testIO.acquire();
		double ndTime= System.currentTimeMillis();
		Log.v(TAG,"done acquiring signal in = "  + (ndTime-stTime)/1000
				+ " secs at playFs= " + super.playbackSampleFrequency
				+ " and recFs= " +super.recordingSampleFrequency);
		Log.v(TAG,"Expected duration of at least = "+ stimulusDuration + " secs" );
		File file= AudioPulseFileWriter.generateFileName("TEOAE","",super.testEar);
		fileNames.add(file.getAbsolutePath());
		fileNamestoDataMap.put(file.getAbsolutePath(),
				AudioPulseDataAnalyzer.RAWDATA_CLICK);
        sendMessage(TestActivity.Messages.IO_COMPLETE); //Not exactly true because we delegate writing of file to another thread...
		
        try {
			DPGRAM = analyzeResults(results,super.recordingSampleFrequency);
		} catch (Exception e) {
			Log.e(TAG,"Could not generate analysis for results!!" + e.getMessage());
			e.printStackTrace();
		}
		
		//TODO: Send data back to Activity, the final saving of result will be done when the Activity returns from plotting the processed
		//data and the user accepts the results. 
		data=new Bundle();
		data.putSerializable(AudioPulseDataAnalyzer.Results_MAP,DPGRAM);
		data.putSerializable(AudioPulseDataAnalyzer.RAWDATA_CLICK,results);
		
		//The file passed here is not used in the analysis (ie not opened and read)!!
		//It is used when the analysis gets accepted by the user: the app packages
		//the file with stored the data for transmission with timestamp on the file name
		data.putSerializable(AudioPulseDataAnalyzer.MetaData_RawFileNames,fileNames);
		data.putSerializable(AudioPulseDataAnalyzer.FileNameRawData_MAP,fileNamestoDataMap);
		Log.v(TAG,"Sending analyzed data to activity");
		sendMessage(TestActivity.Messages.ANALYSIS_COMPLETE,data);
		Log.v(TAG,"donew with " + TAG);
	}
	
	private HashMap<String, Double> analyzeResults(short[] data, int Fs) throws Exception {
		int epochSize=(int) Math.round(Signals.getclickKempSweepDurationSeconds()*Fs); //Size in samples of the trial
		Log.v(TAG,"N= " + data.length + " data time= " + (data.length/Fs) + 
				" Fs=" + Fs + " epochSize= " + epochSize);	
		if((data.length/Fs)< stimulusDuration){
			Log.e(TAG,"Data is too short, size is: " + (data.length/Fs) +
					" but we expected: " + stimulusDuration);
		}
		AudioPulseDataAnalyzer teoaeAnalysis=new TEOAEKempAnalyzer(data,Fs,epochSize);
		return teoaeAnalysis.call();

	}

}
