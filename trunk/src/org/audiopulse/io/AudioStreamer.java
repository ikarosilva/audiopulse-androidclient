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
 * Original Author:  Andrew Schwartz
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.io;

import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;
import org.audiopulse.utilities.ThreadedSignalGenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioStreamer
{
	public static final String TAG = "AudioStreamer";
	public final int sampleRate;
	
	public static final int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
	public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	public static final int audioMode = AudioManager.STREAM_MUSIC;
	public static final int trackMode = AudioTrack.MODE_STREAM;
			
	private ThreadedSignalGenerator source = null;
	private Thread playThread;
	private boolean requestStop;	//TODO: volatile?
	private AudioTrack track;
	private final int frameLength;		//num mono samples per audio frame
	
	//default buffer values constructor
	public AudioStreamer(int sampleRate) {
		this(sampleRate, 0);
	}
	//general constructor
	public AudioStreamer (int sampleRate, int frameLength)
	{
		int bytesPerFrame = frameLength * 2 * 2;		//2 bytes per sample, 2 buffer samples per stereo signal sample
		int minBufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
		assert false : "Test assertion";
		if (bytesPerFrame < minBufferSizeInBytes) {
			if (frameLength != 0)		//use 0 for min buffer size
				Log.w(TAG, "Buffer size too short, using minBufferSize instead");
			assert minBufferSizeInBytes % 4==0 : "minBufferSize is either not in bytes or not for stereo buffers" ;
			frameLength = minBufferSizeInBytes / 4;
			bytesPerFrame = frameLength * 2 * 2;
		}
		
		this.sampleRate = sampleRate;
		this.frameLength = frameLength;
		
		track = new AudioTrack(audioMode, sampleRate, channelConfig, audioFormat, bytesPerFrame, trackMode); 
		if(track.getState() != AudioTrack.STATE_INITIALIZED) {
			Log.e(TAG,"Error: Audio record was not properly initialized!!");
			return;
		}
		
	}
	
	public void attachSource(ThreadedSignalGenerator source) {
		if (source.getBufferLength() != frameLength)
			throw new IllegalArgumentException("Source bufferLength must match AudioStreamer frameLength");
		
		this.source = source;
	}

	public void start()
	{
		Log.v(TAG,"Stream start");
		track.play();
		
		//create thread to play
		
		playThread = new Thread( new Runnable( ) {
			public void run( ) {
				requestStop = false;
				while(!requestStop) {
					double[] frame = null;
					while (!requestStop && frame==null)
						frame = source.getBuffer();	//gets current buffer, tells source to generate next buffer
					Log.v(TAG,"Writing audio frame");
					short[] writeData = AudioSignal.getAudioTrackData(frame, true);
					int nWritten = track.write(writeData,0,writeData.length);
					if (nWritten == AudioTrack.ERROR_INVALID_OPERATION || nWritten == AudioTrack.ERROR_BAD_VALUE) {
						Log.e(TAG, "Audio write failed: " + nWritten);
						break;
					}
					Log.v(TAG,"Frame written successfully");
				}
			}
		} );
		playThread.setPriority(Thread.MAX_PRIORITY);
		playThread.start();
		
	}
	public void stop() {
		Log.v(TAG,"AudioStreamer Stop");
		requestStop = true;
		track.pause();
		track.flush();
		//track.release();	//don't really know if this is necessary (or sufficient)
	}
	
	public int getFrameLength() {
		return frameLength;
	}

}












