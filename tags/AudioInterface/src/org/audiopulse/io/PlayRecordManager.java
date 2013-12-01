package org.audiopulse.io;

import java.io.IOException;

import org.audiopulse.hardware.AcousticConverter;
import org.audiopulse.utilities.AudioSignal;
import org.audiopulse.utilities.SignalProcessing;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

//TODO: allow checking of threads' status, interrupting, etc.
public class PlayRecordManager {
	private static final String TAG = "PlayRecordManager";

	private AudioTrack player;
	private AudioRecord recorder;
	private boolean recordingEnabled = false;
	private boolean playbackEnabled = false;
	
	private int playbackSampleRate;
	private int recordingSampleRate;
	private int playerBufferLengthInMillis = 200;		//length of playback buffer (not stimulus)
	private int recorderBufferLengthInMillis = 200;		//length of recording buffer (not read size)
	private int recorderReadLengthInMillis = 50;		//recording buffer read length per operation
	private int playerBufferLength;
	private int recorderBufferLength;
	private int recorderReadLength;
	
	private int playerMode;						//MODE_STATIC or MODE_STREAM
	private volatile int numSamplesPlayed;				//# samples played so far
	private volatile int numSamplesRecorded;				//# sample recorded so far
	private int numSamplesToRecord;				//total # samples to record
	
	private short[] stimulusData;			//playback data
	private short[] recordedData;			//short buffer to write recorded samples to
	private final int recordingPadInMillis = 100;			//extra time (ms) for recording buffer to cover sync issues (not necessarily used)
	
	private volatile boolean stopRequest;
	private volatile boolean playbackStarted;
	private volatile boolean playbackCompleted;
	private volatile boolean recordingStarted;
	private volatile boolean recordingCompleted;

	private Thread playbackThread = new Thread();
	private Thread recordingThread = new Thread();
	private Object playbackLock = new Object();
	private Object recordingLock = new Object();
	private Object playRecordSyncLock = new Object();
	
	public AudioRecord.OnRecordPositionUpdateListener recordListener;
	
	//creator for just playback or recording
	public PlayRecordManager(int sampleFrequency) {
		//set sampleFreq as both play and record
		this(sampleFrequency,sampleFrequency);
	}
	//creator for playback and recording 
	public PlayRecordManager(int playbackSampleFrequency, int recordingSampleFrequency) {
		this.playbackSampleRate = playbackSampleFrequency;
		this.recordingSampleRate = recordingSampleFrequency;
		Log.v(TAG,"playbackSampleRate= " + playbackSampleRate 
				+ " recordingSampleRate= " + recordingSampleRate);
		playerBufferLength = playerBufferLengthInMillis * playbackSampleFrequency / 1000;
		recorderBufferLength = recorderBufferLengthInMillis * playbackSampleFrequency / 1000;
		recorderReadLength = recorderReadLengthInMillis * playbackSampleFrequency / 1000;

		Log.v(TAG,"playerBufferLength= " + playerBufferLength 
				+ " recordingSampleRate= " + recordingSampleRate);
		Log.v(TAG,"playbackSampleRate= " + playbackSampleRate 
				+ " recorderBufferLength= " + recorderBufferLength);
		Log.v(TAG,"recorderReadLength= " + recorderReadLength);
		
		//TODO: create AudioTrack and AudioRecord objects here?
	}
	
	//TODO: have public functions return success, or possibly throw exceptions
	
	
	//set to playback only, specify stimulus
	public synchronized void setPlaybackOnly(short[] stimulus) {
		short[] safeStimulus = stimulus.clone();
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = true;
				this.recordingEnabled = false;
				initializePlayback(playbackSampleRate, safeStimulus);
				
			}
		}		
	}
	
	//set to playback and record, specify stimulus
	public synchronized void setPlaybackAndRecording(short[] stimulus) {
		short[] safeStimulus = stimulus.clone();
		AcousticConverter hardware = new AcousticConverter();
		Log.v(TAG,"Stimulus set: [0]= " + safeStimulus[0] + " [N]=" + safeStimulus[safeStimulus.length-1]);
		Log.v(TAG,"Stimulus set: rms = " + SignalProcessing.rms(AudioSignal.convertToMono(AudioSignal.convertStereoToDouble(safeStimulus))));
		//FIXME: switch on mono / stereo to determine playback length
		//FIXME: be safer about integer overflows for long buffers!
		//sync recording length to playback length (assumes stereo interleaved)
		int playbackLengthInMillis = stimulus.length/2 *1000 / playbackSampleRate;
		int recordingSampleLength = (playbackLengthInMillis + recordingPadInMillis) * recordingSampleRate / 1000;
		
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = true;
				this.recordingEnabled = true;
				initializePlayback(playbackSampleRate, safeStimulus);
				initializeRecording(recordingSampleRate, recordingSampleLength);
			}
		}
	}
		
	//set to record only, specify recording time
	public synchronized void setRecordingOnly(int recordTimeInMillis) {
		
		int recordingSampleLength = (recordTimeInMillis * recordingSampleRate) / 1000;
		synchronized(playbackLock) {
			synchronized (recordingLock) {
				this.playbackEnabled = false;
				this.recordingEnabled = true;
				initializeRecording(recordingSampleRate, recordingSampleLength);
			}
		}
	}

	//TODO: allow native write result to file?
	
	//start playback and/or recording
	public synchronized short[] acquire() {
		
		//start IO
		if (recordingEnabled) {
			recordingThread.start();
		}
		if (playbackEnabled) {
			playbackThread.start();
		}

		//wait for completion before returning control
		try {
			while (!isIoComplete()) {
				Log.d(TAG,"Waiting for IO completion...");
				this.wait();
				Log.d(TAG,"start() thread woken up, checking if we're done.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.e(TAG,"start thread interrupted");
			return null;
		}
		
		Log.d(TAG,"We're done, returning control.");
		return recordedData.clone();
		
	}
	
	//stop all I/O
	public void stop() {
		Log.i(TAG,"playRecordManager manually stopped!");
		stopRequest = true;
		//TODO: use interrupt on playThread and recordThread, rather than this?
	}
	
	//run within playbackThread to write samples to hardware output buffer
	private void playbackLoop() {
		synchronized (playbackLock) {
			stopRequest = false;
			int numSamplesToWrite = stimulusData.length;
			
			if (recordingEnabled) {
				//wait for recording to start
				try {
					synchronized (playRecordSyncLock) {
						while (!recordingStarted) {
							Log.d(TAG,"Playback loop waiting for record loop");
							playRecordSyncLock.wait();
						}
						Log.d(TAG,"Playback loop received the go!");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e(TAG,"Interrupted while waiting for record loop to start!");
					return;
				}
			}
			
			//start playback loop
			Log.d(TAG,"Starting play loop: " + numSamplesToWrite + " samples to write.");
			playbackStarted = true;
			for (numSamplesPlayed=0; numSamplesPlayed<numSamplesToWrite;) {
				if (stopRequest) {
					break;
				}
				
				int remainingSamples = numSamplesToWrite - numSamplesPlayed;
				int writeSize=(playerBufferLength<=remainingSamples) ? playerBufferLength : remainingSamples;
				int nWritten = player.write(stimulusData, numSamplesPlayed, writeSize);
				if (nWritten==AudioTrack.ERROR_BAD_VALUE || nWritten==AudioTrack.ERROR_INVALID_OPERATION) {
					Log.e(TAG, "Audio write failed: " + nWritten);
					//TODO: throw exception?
					//throw(new IOException("Audio write failed with code " + nWritten));
				}
				numSamplesPlayed+= nWritten;
			}
			player.pause(); player.flush();
			Log.d(TAG,"Done playbackLoop");
			playbackCompleted = true;
			notifyIOComplete();		//notify if all IO is done
		}
	}

	//run within recordingThread to read samples from hardware buffer
	private void recordLoop() {
		synchronized (recordingLock) {
			Log.d(TAG,"Starting record loop: " + numSamplesToRecord + " samples to record.");
			stopRequest = false;
			numSamplesRecorded = 0;
			
			//first, request entire buffer to mark all samples as read
			int nRead = recorder.read(new short[recorderBufferLength],0,recorderBufferLength);
			Log.d(TAG,"Read and discarded initial full buffer");
			
			//inform playback loop that recording has started
			recordingStarted = true;
			if (playbackEnabled) {
				synchronized (playRecordSyncLock) {
					playRecordSyncLock.notify();
					Log.d(TAG,"Telling playback loop to go");
				}
			}
			
			//start main recording loop
			while (!stopRequest) {
				int requestSize = recorderReadLength;
				
				//if playback is enabled & done, so are we.
				//so we will request final buffer (full length)
				if (playbackEnabled && playbackCompleted) {
					requestSize = recorderBufferLength;
					stopRequest = true;
					Log.d(TAG,"Playback loop finished, recording loop to terminate");
				}
				
				//make sure we don't overflow
				int remainingSamples = numSamplesToRecord - numSamplesRecorded;
				if (remainingSamples<recorderReadLength) {
					//There are two ways we might have hit the end of our recordedDataBuffer:
					if (!playbackEnabled) {
						//in this case, we've just reached the end of the requested recording time, and should stop
						requestSize = remainingSamples;
					} else {
						//in this case, something went wrong as the playbackLoop should have already told us to stop
						Log.e(TAG,"Ran out of recording buffer space, IO sync error?");
						//TODO: throw exception
//						throw(new IOException("Play/Record sync error: " +
//											  numSamplesPlayed + " samples played, " + 
//											  numSamplesRecorded + " samples recorded, " + 
//											  numSamplesToRecord + " total samples to record"));
					}
					
				}
			
				//perform read
				nRead=recorder.read(recordedData,numSamplesRecorded,requestSize);
				if (nRead == AudioRecord.ERROR_INVALID_OPERATION || nRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "Audio read failed: " + nRead);
					Log.e(TAG,"Data buffer size: " + recordedData.length);
					Log.e(TAG,"Curent position: " + numSamplesRecorded);
					Log.e(TAG,"Requested read size: " + requestSize);
					
					//TODO: throw exception?
					//throw(new IOException("Audio read failed with code " + nRead));

				}
				numSamplesRecorded += nRead;
			}
			
			recorder.stop();
			
			Log.d(TAG,"Done recordingLoop, recorded " + numSamplesRecorded + " samples.");
			
			//keep only the part of the data buffer that we wrote to
			short[] finishedAudio = new short[numSamplesRecorded];
			System.arraycopy(recordedData, 0, finishedAudio, 0, numSamplesRecorded);
			recordedData = finishedAudio;
			recordingCompleted = true;
			notifyIOComplete(); 	//notify this if all IO is done
		}
	}
	
	//Check if play&record are done, notify if so.
	private void notifyIOComplete() {
		if (isIoComplete()) {
			synchronized(this) {
				this.notifyAll();
			}
		}
	}
	
	//determine if play and record (if enabled) are completed
	private boolean isIoComplete() {
		return ((!playbackEnabled || playbackCompleted) &&
				(!recordingEnabled || recordingCompleted));
	}
	
	//initialize everything we need to trigger playback
	private void initializePlayback(int sampleFrequency, short[] stimulus) {
		synchronized (playbackLock) {
			synchronized (recordingLock) {
				
				this.playbackSampleRate = sampleFrequency;
				this.stimulusData = stimulus;
				this.playbackCompleted = false;
				
				if (player!=null) player.release();
				
				//set up AudioTrack object (interface to playback hardware)
				playerMode = AudioTrack.MODE_STREAM;	//TODO: MODE_STATIC? Google bug?
				int minBuffer = AudioTrack.getMinBufferSize(
						playbackSampleRate, 
						AudioFormat.CHANNEL_OUT_STEREO,
						AudioFormat.ENCODING_PCM_16BIT);
				int bufferSizeInBytes = 2 * playerBufferLength;
				if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
				player = new AudioTrack(AudioManager.STREAM_MUSIC,
						  playbackSampleRate,
						  AudioFormat.CHANNEL_OUT_STEREO,
						  AudioFormat.ENCODING_PCM_16BIT,
						  bufferSizeInBytes,
						  playerMode);
				player.play();
				Log.i(TAG,"Player initialized with " + bufferSizeInBytes + "-byte ( " + bufferSizeInBytes*1000/2/playbackSampleRate + "ms) buffer");
				
				if(player.getChannelConfiguration() != AudioFormat.CHANNEL_OUT_STEREO) {
					Log.e(TAG,"Channel channel not stereo (" + 
				AudioFormat.CHANNEL_OUT_STEREO + " ) value is= " + player.getChannelConfiguration());
					throw new AssertionError("Invalid Audio  Channel Configuration");
				}
				
				if(player.getPlaybackRate() != playbackSampleRate) {
					Log.e(TAG,"Channel rate not configure properly (" + 
				playbackSampleRate + " ) playback value is= " + player.getPlaybackRate());
					throw new AssertionError("Invalid Plabyback Rate Configuration");
				}
				if(player.getSampleRate() != playbackSampleRate) {
					Log.e(TAG,"Channel rate not configure properly (" + 
				playbackSampleRate + " ) value is= " + player.getSampleRate());
					throw new AssertionError("Invalid Sample Rate Configuration");
				}
				
				Log.i(TAG,"Audio Track configuration: sampleRate= " + player.getSampleRate()
						+ " ChannelConfiguration= " + player.getChannelConfiguration() 
						+ " PlaybackRate" + player.getPlaybackRate());
				
				//set up Thread that will run playback loop
				playbackThread = new Thread( new Runnable() {
					public void run() {
						playbackLoop();
					}
				}, "PlaybackThread");
			}
		}
	}
	
	//initialize everything we need to trigger recording
	private void initializeRecording(int sampleFrequency, int recordingSampleLength) {
		synchronized (playbackLock) {
			synchronized (recordingLock) {
				
				this.recordingSampleRate = sampleFrequency;
				this.numSamplesToRecord = recordingSampleLength;
				recordedData = new short[numSamplesToRecord];
				this.recordingCompleted = false;
				
				if (recorder!=null) recorder.release();
				
				//set up AudioRecord object (interface to recording hardware)
				int minBuffer = AudioRecord.getMinBufferSize(
						recordingSampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT
						);
				int bufferSizeInBytes = 2 * recorderBufferLength;
				if (bufferSizeInBytes < minBuffer) bufferSizeInBytes = minBuffer;
				recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
						recordingSampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT,
						bufferSizeInBytes);
				recorder.startRecording();
				Log.i(TAG,"Recorder initialized with " + bufferSizeInBytes + "-byte ( " + bufferSizeInBytes*1000/2/playbackSampleRate + "ms) buffer");

				//set up thread that will run recording loop
				recordingThread = new Thread( new Runnable() {
					public void run() {
						recordLoop();
					}
				}, "RecordingThread");
			}
		}
	}
	
		
}