package org.audiopulse.analysis;

import java.io.Serializable;

import android.util.Log;

public class DPOAEResults implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7151583590731408124L;
	private static final String TAG="DPOAEResults";
	//AudioPulseDataAnalyzer  interface
	final double respSPL;
	final double noiseSPL;
	final double stim1SPL;
	final double stim2SPL;
	
	final double respHz;
	final short stim1Hz;
	final short stim2Hz;
	
	protected double[] dataFFT;
	protected double[] dataWav;
	protected double[] noiseRangeHz;
	final String fileName;
	final String protocol;
	
	public DPOAEResults(double respSPL,double noiseSPL,double stim1SPL, double stim2SPL, double respHz, 
						short stim1Hz,short stim2Hz,double[] dataFFT,double[] dataWav,
						String fileName,String protocol){	
		this.respSPL=respSPL;
		this.noiseSPL=noiseSPL;
		this.stim1SPL=stim1SPL;
		this.stim2SPL=stim2SPL;	
		this.respHz=respHz;
		this.stim1Hz=stim1Hz;
		this.stim2Hz=stim2Hz;	
		this.dataFFT=dataFFT;
		this.fileName=fileName;
		this.protocol=protocol;
		this.dataWav=dataWav;
	}
	
	public double getRespSPL(){return respSPL;}
	public double getNoiseSPL(){return noiseSPL;}
	public double getStim1SPL(){return stim1SPL;}
	public double getStim2SPL(){return stim2SPL;}
	public double getRespHz(){return respHz;}
	public short getStim1Hz(){return stim1Hz;}
	public short getStim2Hz(){return stim2Hz;}
	public double[] getDataFFT(){return dataFFT;}
	public void setDataFFT(double[] FFT){dataFFT=FFT;}
	public String getFileName(){return fileName;}
	public String getProtocol(){return protocol;}
	public double[] getWave() { return dataWav;}
	public void setWave(double[] data) { dataWav=data;}
	public double[] getNoiseRangeHz() {return noiseRangeHz;	}
	public void setNoiseRangeHz(double [] x) {noiseRangeHz=x;}

}
