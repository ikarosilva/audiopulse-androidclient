package org.moca.task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask which copies data from a source Uri to a destination Uri within a 
 * context.
 * 
 * Applications must have read and write access to the content otherwise the 
 * task will fail. The CopyTaskRequest passed to the execute() method should
 * have a scheme considered valid by the ContentResolver.openInputStream()
 * method.
 * 
 * @author Eric Winkler
 */
public class CopyTask extends AsyncTask<CopyTaskRequest, Integer, Integer> {
	public static final String TAG = CopyTask.class.getSimpleName();
	/** An error occurred during copying and the copy failed. */ 
	public static final int COPY_ERROR = -1;
	/** A copy operation was successful */
	public static final int COPY_SUCCESS = 0;

	// defaults to failure
	private int result = COPY_ERROR;
	// percent written as int
	private int progress = 0;

	@Override
	protected Integer doInBackground(CopyTaskRequest... params) {
		try{
			final InputStream in = params[0].c.getContentResolver()
											.openInputStream(params[0].source);
			final OutputStream out = params[0].c.getContentResolver()
											 .openOutputStream(params[0].dest);
			try {
				byte[] buffer = new byte[1024];
				int bytesWritten = 0;
				int bytesAvailable = in.available();
				int bytesRemaining = bytesAvailable;
				while(bytesRemaining > 0) {
					int read = in.read(buffer);
					out.write(buffer, 0, read);
					bytesRemaining -= read;
					bytesWritten += read;
		            publishProgress((int)((
		            		bytesWritten/(float) bytesAvailable) * 100));
				}	
				out.flush();
				in.close();
				out.close();
				Log.i(TAG, "CopyTask Successfully copied data");
				return COPY_SUCCESS;
			} catch (IOException e) {
				Log.i(TAG, "IOerror");
			} 
		} catch (FileNotFoundException e){
			Log.i(TAG, "File not found ");
		}
		return COPY_ERROR;
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "About to copy");
	}

	@Override
	protected void onPostExecute(Integer result) {
		Log.d(TAG, "Completed copy");
		this.result = result;
	}

	@Override
	protected void onProgressUpdate(Integer ...progress)
	{
		setProgressPercent(progress[0]);
	}

	private void setProgressPercent(Integer completed) {
		progress = completed;
	}
}
