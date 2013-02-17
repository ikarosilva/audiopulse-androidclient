package org.audiopulse.utilities;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.audiopulse.io.ShortFile;
import org.audiopulse.utilities.SignalProcessing;

import android.util.Log;


class DPOAEAnalysisException extends Exception {
	private static final long serialVersionUID = 1L;
	public DPOAEAnalysisException() {
	}
	public DPOAEAnalysisException(String msg) {
		super(msg);
	}
}

public class DPOAEAnalysis {

	static boolean headless = false;
	static final String TAG="DPOAEAnalysis";

	public static void setHeadless(boolean headless){
		DPOAEAnalysis.headless = headless; 
	}

	public static double[][] getSpectrum(short[] x, double Fs, int epochTime){
		return SignalProcessing.getSpectrum(x, Fs,epochTime);
	}

	public static double[] getResponse(double[][] XFFT, double desF, double tolerance){

		//Search through the spectrum to get the closest bin 
		//to the respective frequencies
		double dminF=Short.MAX_VALUE;
		double dF; 
		//Results will be stored in a vector where first row is the closest
		//bin from the FFT wrt the frequency and second row is the power in that
		//bin. 
		double[] result=new double[2];
		for(int n=0;n<XFFT[0].length;n++){
			dF=Math.abs(XFFT[0][n]-desF);
			if( dF < dminF ){
				dminF=dF;
				result[0]=n;
				result[1]=XFFT[1][n];
			}		
		}
		if(dminF > tolerance){
			double  actF=XFFT[0][(int)result[0]];
			System.err.println("Results are innacurate because frequency tolerance has been exceeded. Desired F= "
					+ desF +" closest F= " + actF);
		}
		//System.out.println("F= "+ desF +" closest F= " + XFFT[0][(int)result[0]]);
		return result;
	}

	public static double getNoiseLevel(double[][] XFFT, int Find){

		//Estimates noise by getting the average level of 3 frequency bins above and below
		//the desired response frequency (desF)
		double noiseLevel=0;	
		//Get the average from 3 bins below and 3 bins above
		for(int i=0;i<=6;i++){
			if(i !=3){
				noiseLevel+= XFFT[1][(Find+i-3)];
			}
		}
		return (noiseLevel/6);
	}

	public static File[] finder( String dirName){
		File dir = new File(dirName);
		return dir.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename)
			{ return filename.endsWith(".raw"); }
		} );
	}

	public static  ArrayList<Double[]> runAnalysis(String dataDir) {

		//Set parameters according to the procedures defined by
		//Gorga et al 1993,"Otoacoustic Emissions from Normal-hearing and hearing-impaired subject: distortion product responses
		double Fs=16000, F2=0, F1=0,Fres=0;	 //Frequency of the expected response
		int epochTime=512; //Size of each epoch from which to do the averaging, this is a trade off between
		//being close the Gorga's value (20.48 ms) and being a power of 2 for FFT analysis and given our Fs. 
		File[] oaeFiles=finder(dataDir);
		Arrays.sort(oaeFiles);
		double[] results = new double[3]; 
		double tolerance=50; //Tolerance, in Hz, from which to get the closest FFT bin relative to the actual desired frequency
		int M=3, fIndex=0; //number of frequencies being tested
		//The data is sent for plotting in an interleaved fashion
		//where odd elements are x-axis and even elements are y-axis
		Double[] DPOAEData=new Double[2*M];
		Double[] noiseFloor=new Double[2*M];
		Double[] f1Data=new Double[2*M];
		Double[] f2Data=new Double[2*M];
		double[] tmpResult=new double[2];
		int FresIndex;
		short[] rawData=null;
		ArrayList<Double[]> finalData=new ArrayList<Double []>();

		for(int i=0;i<oaeFiles.length;i++){
			String outFileName=oaeFiles[i].getAbsolutePath();		
			//TODO: Right now the analysis is based on the Handbook of Otoacoustic Emissions Book by Hall
			//These parameters (F1,F2,Fres) should be loaded dynamically based on the protocol description
			//on the acompanying XML File
			Log.v(TAG,"Running analysis for: " + outFileName);
			if(outFileName.contains("AP_DPGram-Left-Ear-2kHz")){
				F2=2000;F1=F2/1.2;Fres=(2*F1)-F2;	
				fIndex=0*2;//index are all even, data amp goes in the odd indeces
			}else if(outFileName.contains("AP_DPGram-Left-Ear-3kHz")){
				F2=3000;F1=F2/1.2;Fres=(2*F1)-F2;
				fIndex=1*2;
			}else if(outFileName.contains("AP_DPGram-Left-Ear-4kHz")){
				F2=4000;F1=F2/1.2;Fres=(2*F1)-F2;
				fIndex=2*2;
			}else{
				Log.v(TAG,"Unexpected DPOAE File Name: " +  outFileName);
			}
			Log.v(TAG,"Reading file: " + oaeFiles[i].getAbsolutePath());
			try {
				rawData = ShortFile.readFile(oaeFiles[i].getAbsolutePath());
			} catch (Exception e) {
				Log.v(TAG,"Could not read data from file: " +  e.getMessage());
				e.printStackTrace();
			}
			
			//Check to see if any clipping occurred
			if(SignalProcessing.isclipped(rawData,Fs)){
				Log.v(TAG,"Error: clipping occured in:" + outFileName);
				System.err.println("Error: clipping occured in:" + outFileName);
			}	
			Log.v(TAG,"Estimating spectrum");
			double[][] XFFT= DPOAEAnalysis.getSpectrum(rawData,Fs,epochTime);
			tmpResult=getResponse(XFFT,F1,tolerance);
			results[0]=tmpResult[1];

			tmpResult=getResponse(XFFT,F2,tolerance);
			results[1]=tmpResult[1];

			tmpResult=getResponse(XFFT,Fres,tolerance);
			results[2]=tmpResult[1];
			FresIndex =(int) tmpResult[0]; //the closest FFT bin to the desired frequency that we want

			f1Data[fIndex]=F2;
			f1Data[fIndex+1]=Double.valueOf(Math.round(results[0]));

			f2Data[fIndex]=F2;
			f2Data[fIndex+1]=Double.valueOf(Math.round(results[1]));

			DPOAEData[fIndex]=F2;
			DPOAEData[fIndex+1]=Double.valueOf(Math.round(results[2]));

			Log.v(TAG,"Estimating noise floor");
			noiseFloor[fIndex]=F2;
			noiseFloor[fIndex+1]=getNoiseLevel(XFFT,FresIndex);
			//Delete the file once they have been compressed and packed and analyzed
			if(oaeFiles[i].getName().endsWith(".raw")){
				Log.v(TAG,"Deleting temporary file:" +oaeFiles[i].getName());
				oaeFiles[i].delete();
			}

		}	

		//Send data as an double array
		//Log.v(TAG,"f1[0]=" + f1Data[0]+"f1[1]=" + f1Data[1]+"f1[2]=" + f1Data[2]);
		Log.v(TAG,"sending results back");
		finalData.add(f1Data);
		finalData.add(f2Data);
		finalData.add(DPOAEData);
		finalData.add(noiseFloor);
		return finalData;

	}
}


















