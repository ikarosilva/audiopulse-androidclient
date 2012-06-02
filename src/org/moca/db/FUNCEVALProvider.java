package org.moca.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import org.moca.db.MocaDB.FUNCEVALSQLFormat;
import org.moca.db.MocaDB.DatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * Simple ContentProvider which stores the file references in a "_data" 
 * column and may also hold a thumbnail reference.
 *
 * @author Eric Winkler
 */
public class FUNCEVALProvider extends ContentProvider {
    private static final String TAG = FUNCEVALProvider.class.getSimpleName();
    
    public static final String VIEW_PARAMETER = "view";
    public static final String THUMBNAIL_VIEW = "thumb";
    
    private static final String TABLE_NAME = "funceval";
    
    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;
    
    // duplicate here to make porting the rest of the code easier
    private static final Uri CONTENT_URI = FUNCEVALSQLFormat.CONTENT_URI;
    private static final String CONTENT_TYPE = FUNCEVALSQLFormat.CONTENT_TYPE;
    private static final String CONTENT_ITEM_TYPE = 
    	FUNCEVALSQLFormat.CONTENT_ITEM_TYPE;
    private static final String DEFAULT_SORT_ORDER = 
    	FUNCEVALSQLFormat.DEFAULT_SORT_ORDER;
    
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sFUNCEVALProjectionMap;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }
    

    /** @hide */
    protected boolean deleteFileHelper(long id) {
    	return deleteDefaultFileHelper(id) && 
    		deleteFileHelper(ContentUris.withAppendedId(CONTENT_URI, id));
    }
    
    /**
     * Convenience for subclasses that wish to implement {@link #openFile}
     * by looking up a column named "_data" at the given URI.
     *
     * @param uri The URI to be opened.
     *
     * @return true if a File was deleted.
     */
    protected boolean deleteFileHelper(Uri uri) {
            Cursor c = query(uri, new String[]{"_data"}, null, null, null);
            int count = (c != null) ? c.getCount() : 0;
            if (count != 1) {
                // If there is not exactly one result, throw an appropriate
                // exception.
                if (c != null) {
                    c.close();
                }
                return false;
            }
            
            c.moveToFirst();
            int i = c.getColumnIndex("_data");
            String path = (i >= 0 ? c.getString(i) : null);
            c.close();
            if (path == null) {
               return false;
            }
            File f = new File(path);
            return f.delete();
    }
    
    protected boolean deleteDefaultFileHelper(long id){
    	File f  = new File(getContext().getFilesDir(), "funceval_"+id);
    	return f.delete();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    	return openFileHelper(uri, mode);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) 
    {    
    	Log.i(TAG, "in query at funcevalprovider");
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        
        switch(sUriMatcher.match(uri)) {
        case ITEMS:    
            break;
        case ITEM_ID:
            qb.appendWhere(FUNCEVALSQLFormat._ID + "=" + uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        Log.i(TAG, "query(): " + uri );
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0; 
        
        switch(sUriMatcher.match(uri)) {
        case ITEMS:
            count = db.update(TABLE_NAME, values, selection, selectionArgs);
            break;
            
        case ITEM_ID:
            String id = uri.getPathSegments().get(1);
            count = db.update(TABLE_NAME, values, 
            		FUNCEVALSQLFormat._ID + "=" + id + 
            		(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
            		selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ITEMS:
        	LinkedList<Long> files = new LinkedList<Long>();
        	Cursor c = query(CONTENT_URI, 
        			new String[] {BaseColumns._ID }, 
        			selection, 
        			selectionArgs, 
        			null);
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String id = c.getString(0);
        			files.add(Long.valueOf(id));
        			c.moveToNext();
        		}
        	}
        	if(c != null)
        		c.close();
            Log.i(TAG, "Preparing to delete  " + files.size()+" files");
            // loop over the uris and remove the file stored at path "_data"
            for(long id: files){
            	boolean result = deleteFileHelper(id);
                Log.i(TAG, "delete file: " + uri+", result: " + result );
            }
        	
            count = db.delete(TABLE_NAME, selection, selectionArgs);
            break;
        case ITEM_ID:
            String id = uri.getPathSegments().get(1); 
            Log.i(TAG, "Preparing to delete  file for: " + uri);
        	boolean result = deleteFileHelper(Long.valueOf(id));
            Log.i(TAG, "delete file: " + uri+", result: " + result );
            count = db.delete(TABLE_NAME, 
            		FUNCEVALSQLFormat._ID + "=" + id + 
            		(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
            		selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        Log.i(TAG, "delete(): " + uri  + ", count: " + count);
        return count;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != ITEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues values;
        
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        // REQUIRED
        if(values.containsKey(FUNCEVALSQLFormat.FUNCEVAL_CONTENT_TYPE) == false) {
            values.put(FUNCEVALSQLFormat.FUNCEVAL_CONTENT_TYPE,  "application/octet-stream");
        }

        Long now = Long.valueOf(System.currentTimeMillis());        
        if(values.containsKey(FUNCEVALSQLFormat.CREATED_DATE) == false) {
            values.put(FUNCEVALSQLFormat.CREATED_DATE, now);
        }
        
        if(values.containsKey(FUNCEVALSQLFormat.MODIFIED_DATE) == false) {
            values.put(FUNCEVALSQLFormat.MODIFIED_DATE, now);
        }

        if(values.containsKey(FUNCEVALSQLFormat.DATA) == false) {
            values.put(FUNCEVALSQLFormat.DATA, "");
        } 
 
        if(values.containsKey(FUNCEVALSQLFormat.ENCOUNTER_GUID) == false) {
            values.put(FUNCEVALSQLFormat.ENCOUNTER_GUID, "");
        }
        
        if(values.containsKey(FUNCEVALSQLFormat.ELEMENT_ID) == false) {
            values.put(FUNCEVALSQLFormat.ELEMENT_ID, "");
        }
        
        if(values.containsKey(FUNCEVALSQLFormat.FUNCEVAL_VALID) == false) {
            values.put(FUNCEVALSQLFormat.FUNCEVAL_VALID, false);
        }
        
        if(values.containsKey(FUNCEVALSQLFormat.UPLOAD_PROGRESS) == false) {
            values.put(FUNCEVALSQLFormat.UPLOAD_PROGRESS, 0);
        }
        
        if(values.containsKey(FUNCEVALSQLFormat.UPLOADED) == false) {
            values.put(FUNCEVALSQLFormat.UPLOADED, false);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, 
        		FUNCEVALSQLFormat.ENCOUNTER_GUID, values);

        Log.i(TAG, "insert(): " + uri  + ", rowId: " + rowId);
        
        if(rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            try {
				getContext().openFileOutput("funceval_" + rowId, Context.MODE_PRIVATE).close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        
        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri uri) {
        Log.i(TAG, "getType(uri="+uri.toString()+")");
        switch(sUriMatcher.match(uri)) {
        case ITEMS:
            return CONTENT_TYPE;
        case ITEM_ID:
            return CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    /** 
     * Creates the table in a SQLLite database
     * 
     * @param db the database to create the table in
	 */
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating FUNCEVAL Table");
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + FUNCEVALSQLFormat._ID + " INTEGER PRIMARY KEY,"
                + FUNCEVALSQLFormat.ENCOUNTER_GUID + " TEXT,"
                + FUNCEVALSQLFormat.ELEMENT_ID + " TEXT,"
                + FUNCEVALSQLFormat.FUNCEVAL_VALID + " INTEGER,"
                + FUNCEVALSQLFormat.FUNCEVAL_CONTENT_TYPE + " TEXT,"
                + FUNCEVALSQLFormat.UPLOAD_PROGRESS + " INTEGER,"
                + FUNCEVALSQLFormat.UPLOADED + " INTEGER,"
                + FUNCEVALSQLFormat.CREATED_DATE + " INTEGER,"
                + FUNCEVALSQLFormat.MODIFIED_DATE + " INTEGER,"
                + FUNCEVALSQLFormat.DATA + " TEXT"
                + ");");
    }
    
    /**
     * Drops and then creates the table in a database
     * 
     * @param db the database to drop and create in
     * @param oldVersion
     * @param newVersion
     */
    public static void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);
        if (oldVersion < 3) {
        	// Before version 3, this table didn't exist, so create the table
        	onCreateDatabase(db);
        } else {
        	// Do nothing
        }
    }

	/**
	 * Return a thumbnail Uri for a given FUNCEVALProvider item Uri. The returned
	 * Uri will still be compatible with all ContentProvider methods and refers
	 * to the same item that funcevalUri refers to.
	 */
    public static Uri getThumbUri(Uri funcevalUri) {
    	return getViewUri(funcevalUri, THUMBNAIL_VIEW);
    }
    
    /**
     * Append a view parameter to a funceval Uri
     */
    public static Uri getViewUri(Uri funcevalUri, String viewName) {
    	Uri.Builder uriBuilder = funcevalUri.buildUpon();
    	uriBuilder.appendQueryParameter(VIEW_PARAMETER, viewName);
    	return uriBuilder.build();
    }
    
    /**
     * Convenience wrapper which will try to open the file stored in the "_data" column or
     * will create a new empty file if it fails.
     * 
     * @param cr a {@link android.content.ContentResolver} for the application 
     * 		{@link Context} 
     * @param uri The URI to be opened.
     * @param mode The file mode.  May be "r" for read-only access,
     * 		"w" for write-only access (erasing whatever data is currently in
     * 		the file), "wa" for write-only access to append to any existing data,
     * 		"rw" for read and write access on any existing data, and "rwt" for read
     * 		and write access that truncates any existing file.
     *      
     * @return Returns a new ParcelFileDescriptor that can be used by the
     * client to access the file.
     * @throws FileNotFoundException 
     */
    public static final ParcelFileDescriptor openFileOrCreate(Context c, Uri uri, 
    		String mode) throws FileNotFoundException{
    	ParcelFileDescriptor pfd = null;
    	String prefix = "funceval_";
    	switch(sUriMatcher.match(uri)){
    	case ITEMS:
            throw new IllegalArgumentException("Unknown URI " + uri);
        case ITEM_ID:
    	try{
    		pfd = c.getContentResolver().openFileDescriptor(uri, mode);
    	} catch(FileNotFoundException e){
    		String id = uri.getLastPathSegment();
    		// from android.content.ContentResolver
    		int modeBits;
            if ("r".equals(mode)) {
                modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
            } else if ("w".equals(mode) || "wt".equals(mode)) {
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                        | ParcelFileDescriptor.MODE_CREATE
                        | ParcelFileDescriptor.MODE_TRUNCATE;
            } else if ("wa".equals(mode)) {
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                        | ParcelFileDescriptor.MODE_CREATE
                        | ParcelFileDescriptor.MODE_APPEND;
            } else if ("rw".equals(mode)) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                        | ParcelFileDescriptor.MODE_CREATE;
            } else if ("rwt".equals(mode)) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                        | ParcelFileDescriptor.MODE_CREATE
                        | ParcelFileDescriptor.MODE_TRUNCATE;
            } else {
                throw new FileNotFoundException("Bad mode for " + uri + ": "
                        + mode);
            }
            pfd = ParcelFileDescriptor.open(new File(prefix + id), modeBits);
            String path = 
            	String.format("%s/%s%s",c.getFilesDir().getAbsolutePath(),prefix, id);
        	ContentValues values = new ContentValues();
        	values.put(FUNCEVALSQLFormat.DATA, path);
            c.getContentResolver().update(uri, values, null, null);
    	}
    	return pfd;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MocaDB.FUNCEVAL_AUTHORITY, "funceval", ITEMS);
        sUriMatcher.addURI(MocaDB.FUNCEVAL_AUTHORITY, "funceval/#", ITEM_ID);
                
        sFUNCEVALProjectionMap = new HashMap<String, String>();
        sFUNCEVALProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
        // Class specific past here
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.ENCOUNTER_GUID, FUNCEVALSQLFormat.ENCOUNTER_GUID);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.ELEMENT_ID, FUNCEVALSQLFormat.ELEMENT_ID);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.FUNCEVAL_VALID, FUNCEVALSQLFormat.FUNCEVAL_VALID);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.FUNCEVAL_CONTENT_TYPE, FUNCEVALSQLFormat.FUNCEVAL_CONTENT_TYPE);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.UPLOAD_PROGRESS, FUNCEVALSQLFormat.UPLOAD_PROGRESS);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.UPLOADED, FUNCEVALSQLFormat.UPLOADED);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.CREATED_DATE, FUNCEVALSQLFormat.CREATED_DATE);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.MODIFIED_DATE, FUNCEVALSQLFormat.MODIFIED_DATE);
        sFUNCEVALProjectionMap.put(FUNCEVALSQLFormat.MODIFIED_DATE, FUNCEVALSQLFormat.DATA);
    }
	
}