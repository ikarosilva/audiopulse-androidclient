/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.utilities;

public class PeriodicSeries {


	public double[] frequency;
	public double[] amplitude;
	private double[] increment;
	private short[] data;
	public short N;
	public double Fs; //Sampling frequency in Hz

	public PeriodicSeries(short N,double Fs,double[] frequency){
		this.N=N;
		this.Fs=Fs;
		this.frequency=frequency;
		this.data=new short[N];
		for (int i=0;i<frequency.length;i++){
			this.amplitude[i]=1; //Set default amplitudes in intensity
		}
	}
	
	//Constructor for a DPOAE signal, which is a Periodic Series
	public PeriodicSeries(short N,double Fs,DPOAESignal dpoae){
		this.N=N;
		this.Fs=Fs;
		this.frequency=dpoae.getStimulusFrequency();
		this.amplitude=dpoae.getStimulusAmplitude();
		this.data=new short[N];
	}

	public PeriodicSeries(short N,double Fs,double[] frequency,double[] amplitude){
		this.N=N;
		this.Fs=Fs;
		this.frequency=frequency;
		this.data=new short[N];
		this.amplitude=amplitude; //Amplitudes are in intensity!!
	}

	public short[] generatePeriodicSeries(){

		this.increment=new double[frequency.length];
		this.amplitude=new double[frequency.length];
		double tmpSample;
		double PI2=2*Math.PI;
		double normalizingFactor=0;

		for (int i=0;i<frequency.length;i++){
			this.increment[i] = PI2 * this.frequency[i] /this.Fs; // angular increment for each sample
			normalizingFactor +=amplitude[i];
		}
		for( int i = 0; i < this.N; i++ )
		{
			tmpSample=0;
			for( int k = 0; k < this.frequency.length; k++ )
			{
				tmpSample += this.amplitude[k]*Math.sin(this.increment[k]*i);
			}
			this.data[i]=(short) (Short.MAX_VALUE*tmpSample/normalizingFactor);
		}
		return this.data;
	}
	
	
	
	
	
	
} //of Class definition
