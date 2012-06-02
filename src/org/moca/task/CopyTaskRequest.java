package org.moca.task;

import android.content.Context;
import android.net.Uri;

/**
 * Wrapper for holding information required to copy content from a source Uri to
 * a destination Uri.
 * 
 * @author Eric Winkler
 */
public class CopyTaskRequest {
	/** The Context which will be used to resolve the content */
	public final Context c;
	/** Content Uri which will be copied from. */
	public final Uri source;
	/** Content Uri which will be copied to. */
	public final Uri dest;
	
	/**
	 * Instantiates a new CopyTaskRequest object.
	 * 
	 * @param c The Context which will be used to resolve the Content
	 * @param source Content Uri to Copy from
	 * @param dest Content Uri to copy to
	 */
	public CopyTaskRequest(Context c, Uri source, Uri dest)
	{
		this.c = c;
		this.source = source;
		this.dest = dest;
	}
}
