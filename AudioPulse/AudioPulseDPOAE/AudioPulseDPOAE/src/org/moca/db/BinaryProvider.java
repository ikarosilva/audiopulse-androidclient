package org.moca.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import org.moca.db.MocaDB.BinarySQLFormat;
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
public class BinaryProvider extends ContentProvider {
    private static final String TAG = BinaryProvider.class.getSimpleName();
    
    public static final String VIEW_PARAMETER = "view";
    public static final String THUMBNAIL_VIEW = "thumb";
    
    private static final String TABLE_NAME = "binaries";
    
    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;
    
    // duplicate here to make porting the rest of the code easier
    private static final Uri CONTENT_URI = BinarySQLFormat.CONTENT_URI;
    private static final String CONTENT_TYPE = BinarySQLFormat.CONTENT_TYPE;
    private static final String CONTENT_ITEM_TYPE = 
    	BinarySQLFormat.CONTENT_ITEM_TYPE;
    private static final String DEFAULT_SORT_ORDER = 
    	BinarySQLFormat.DEFAULT_SORT_ORDER;
    
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sBinaryProjectionMap;

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
    	File f  = new File(getContext().getFilesDir(), "binary_"+id);
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
    	
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        
        switch(sUriMatcher.match(uri)) {
        case ITEMS:    
            break;
        case ITEM_ID:
            qb.appendWhere(BinarySQLFormat._ID + "=" + uri.getPathSegments().get(1));
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
            		BinarySQLFormat._ID + "=" + id + 
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
            		BinarySQLFormat._ID + "=" + id + 
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
        if(values.containsKey(BinarySQLFormat.BINARY_CONTENT_TYPE) == false) {
            values.put(BinarySQLFormat.BINARY_CONTENT_TYPE,  "application/octet-stream");
        }

        Long now = Long.valueOf(System.currentTimeMillis());        
        if(values.containsKey(BinarySQLFormat.CREATED_DATE) == false) {
            values.put(BinarySQLFormat.CREATED_DATE, now);
        }
        
        if(values.containsKey(BinarySQLFormat.MODIFIED_DATE) == false) {
            values.put(BinarySQLFormat.MODIFIED_DATE, now);
        }

        if(values.containsKey(BinarySQLFormat.DATA) == false) {
            values.put(BinarySQLFormat.DATA, "");
        } 
 
        if(values.containsKey(BinarySQLFormat.ENCOUNTER_GUID) == false) {
            values.put(BinarySQLFormat.ENCOUNTER_GUID, "");
        }
        
        if(values.containsKey(BinarySQLFormat.ELEMENT_ID) == false) {
            values.put(BinarySQLFormat.ELEMENT_ID, "");
        }
        
        if(values.containsKey(BinarySQLFormat.BINARY_VALID) == false) {
            values.put(BinarySQLFormat.BINARY_VALID, false);
        }
        
        if(values.containsKey(BinarySQLFormat.UPLOAD_PROGRESS) == false) {
            values.put(BinarySQLFormat.UPLOAD_PROGRESS, 0);
        }
        
        if(values.containsKey(BinarySQLFormat.UPLOADED) == false) {
            values.put(BinarySQLFormat.UPLOADED, false);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, 
        		BinarySQLFormat.ENCOUNTER_GUID, values);

        Log.i(TAG, "insert(): " + uri  + ", rowId: " + rowId);
        
        if(rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            try {
				getContext().openFileOutput("binary_" + rowId, Context.MODE_PRIVATE).close();
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
        Log.i(TAG, "Creating Binary Table");
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + BinarySQLFormat._ID + " INTEGER PRIMARY KEY,"
                + BinarySQLFormat.ENCOUNTER_GUID + " TEXT,"
                + BinarySQLFormat.ELEMENT_ID + " TEXT,"
                + BinarySQLFormat.BINARY_VALID + " INTEGER,"
                + BinarySQLFormat.BINARY_CONTENT_TYPE + " TEXT,"
                + BinarySQLFormat.UPLOAD_PROGRESS + " INTEGER,"
                + BinarySQLFormat.UPLOADED + " INTEGER,"
                + BinarySQLFormat.CREATED_DATE + " INTEGER,"
                + BinarySQLFormat.MODIFIED_DATE + " INTEGER,"
                + BinarySQLFormat.DATA + " TEXT"
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
	 * Return a thumbnail Uri for a given BinaryProvider item Uri. The returned
	 * Uri will still be compatible with all ContentProvider methods and refers
	 * to the same item that binaryUri refers to.
	 */
    public static Uri getThumbUri(Uri binaryUri) {
    	return getViewUri(binaryUri, THUMBNAIL_VIEW);
    }
    
    /**
     * Append a view parameter to a binary Uri
     */
    public static Uri getViewUri(Uri binaryUri, String viewName) {
    	Uri.Builder uriBuilder = binaryUri.buildUpon();
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
    	String prefix = "binary_";
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
        	values.put(BinarySQLFormat.DATA, path);
            c.getContentResolver().update(uri, values, null, null);
    	}
    	return pfd;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MocaDB.BINARY_AUTHORITY, "binaries", ITEMS);
        sUriMatcher.addURI(MocaDB.BINARY_AUTHORITY, "binaries/#", ITEM_ID);
                
        sBinaryProjectionMap = new HashMap<String, String>();
        sBinaryProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
        // Class specific past here
        sBinaryProjectionMap.put(BinarySQLFormat.ENCOUNTER_GUID, BinarySQLFormat.ENCOUNTER_GUID);
        sBinaryProjectionMap.put(BinarySQLFormat.ELEMENT_ID, BinarySQLFormat.ELEMENT_ID);
        sBinaryProjectionMap.put(BinarySQLFormat.BINARY_VALID, BinarySQLFormat.BINARY_VALID);
        sBinaryProjectionMap.put(BinarySQLFormat.BINARY_CONTENT_TYPE, BinarySQLFormat.BINARY_CONTENT_TYPE);
        sBinaryProjectionMap.put(BinarySQLFormat.UPLOAD_PROGRESS, BinarySQLFormat.UPLOAD_PROGRESS);
        sBinaryProjectionMap.put(BinarySQLFormat.UPLOADED, BinarySQLFormat.UPLOADED);
        sBinaryProjectionMap.put(BinarySQLFormat.CREATED_DATE, BinarySQLFormat.CREATED_DATE);
        sBinaryProjectionMap.put(BinarySQLFormat.MODIFIED_DATE, BinarySQLFormat.MODIFIED_DATE);
        sBinaryProjectionMap.put(BinarySQLFormat.MODIFIED_DATE, BinarySQLFormat.DATA);
    }
	
}
/*
 * package org.moca.db;
 *

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.moca.db.MocaDB.BinarySQLFormat;
import org.moca.db.MocaDB.DatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentResolver;
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
import android.text.TextUtils;
import android.util.Log;

public class BinaryProvider extends ContentProvider {
    private static final String TAG = BinaryProvider.class.getSimpleName();
    
    public static final String VIEW_PARAMETER = "view";
    public static final String THUMBNAIL_VIEW = "thumb";
    
    private static final String BINARY_TABLE_NAME = "binaries";
    public static final String BINARY_BUCKET_NAME = "/sdcard/dcim/moca/";
    
    private static final int BINARIES = 1;
    private static final int BINARY_ID = 2;
    private static final String APP_DIR = "/data/data/org.moca";
    private static final String prefix = "binary_";
    
    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String,String> sBinaryProjectionMap;
    
    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

	/**
	 * Return a thumbnail Uri for a given BinaryProvider item Uri. The returned
	 * Uri will still be compatible with all ContentProvider methods and refers
	 * to the same item that binaryUri refers to.
	 *
    public static Uri getThumbUri(Uri binaryUri) {
    	return getViewUri(binaryUri, THUMBNAIL_VIEW);
    }
    
    /**
     * Append a view parameter to a binary Uri
     *
    public static Uri getViewUri(Uri binaryUri, String viewName) {
    	Uri.Builder uriBuilder = binaryUri.buildUpon();
    	uriBuilder.appendQueryParameter(VIEW_PARAMETER, viewName);
    	return uriBuilder.build();
    }
    
    /** Returns the full path to the generated file name *
    private final String getFilePathFromId(String id) {
    	return String.format("%s/%s", getFilesDir(),  getNameById(id));
    }
    
    /** returns a file name formatted as "binary_" + id *
    private final String getNameById(String id){
    	return prefix+id;
    }
    
    /** returns a file name formatted as "binary_" + id *
    private final String getNameById(long id){
    	return prefix+id;
    }
    
    private File getFilesDir(){
    	return getContext().getFilesDir();
    }
    
    /** return the name of a application directory file *
    private final String buildFilenameFromId(long id) {
    	return getNameById(id);
    }
   
    // convenience wrapper
    private final String buildFilenameFromId(String id) {
    	return buildFilenameFromId(Long.valueOf(id));
    }
    
    /** Get the file name from the Uri *
    private String buildFilenameFromUri(Uri uri) {
    	return buildFilenameFromId(uri.getPathSegments().get(1));
    }
    
    private String getFilePath(Uri uri){
    	String path = null;
    	Cursor c = query(uri, new String[] { BinarySQLFormat.DATA }, null, 
    			null, null);
    	if(c.moveToFirst()) {
    		path = c.getString(0);
    	} 
    	if(c != null)
    			c.close();
    	Log.d(TAG, String.format("getFilePath(%s): path: %s", uri, path));
    	if(TextUtils.isEmpty(path) || (path.contains(APP_DIR)) ){
    				path = buildFilenameFromId(uri.getLastPathSegment());
    	}
    	Log.d(TAG, String.format("getFilePath(): path: %s", path));
    	return path;
    }
    
    private String getOrCreateFile(Uri uri){
    	String path = getFilePath(uri);
    	return (!TextUtils.isEmpty(path)) ? path : 
    		getFilePathFromId(uri.getPathSegments().get(1));
    }
    
    private boolean deleteFile(String id) {
    	String filename = buildFilenameFromId(id);
    	
    	File f = new File(filename);
    	boolean result = f.delete();
    	Log.d(TAG, "Deleting file for id " + id + " : " + filename + " " + 
    			(result ? "succeeded" : "failed"));
    	return result;
    }
    
    private boolean deleteFile(Uri uri) {
    	List<String> segments = uri.getPathSegments();
    	
    	// Invalid URI
    	if (segments.size() != 1) 
    		return true;
    	
    	String binaryId = segments.get(1);
    	return deleteFile(binaryId);
    }
    
    /**
     * {@inheritDoc}
     *
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    	return openFileHelper(uri, mode);
    	/*
    	String filename = "";
    	if(uri.getScheme().equals(ContentResolver.SCHEME_FILE)){
    		filename = uri.getPath();
    	} else {
    		filename = getFilePath(uri);
    	}
        Log.i(TAG, "openFile() for filename: " + filename + " mode: " + mode);
        
        //Hack to get binary to write to database
        int m = ParcelFileDescriptor.MODE_READ_ONLY;
        if ("w".equals(mode)) {
        	m = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE;
        } else if("rw".equals(mode) || "rwt".equals(mode)) {
        	m = ParcelFileDescriptor.MODE_READ_WRITE;
        }
        return ParcelFileDescriptor.open(new File(filename), m);
        *
    }
    
    /**
     * {@inheritDoc}
     *
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query() uri="+uri.toString() + " projection=" + TextUtils.join(",",projection));
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(BINARY_TABLE_NAME);
        
        switch(sUriMatcher.match(uri)) {
        case BINARIES:    
            break;
        case BINARY_ID:
            qb.appendWhere(BinarySQLFormat._ID + "=" + uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = BinarySQLFormat.DEFAULT_SORT_ORDER;
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
     *
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0; 
        
        switch(sUriMatcher.match(uri)) {
        case BINARIES:
            count = db.update(BINARY_TABLE_NAME, values, selection, selectionArgs);
            break;
            
        case BINARY_ID:
            String procedureId = uri.getPathSegments().get(1);
            count = db.update(BINARY_TABLE_NAME, values, 
            		BinarySQLFormat._ID + "=" + procedureId + 
            		(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
            		selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        Log.i(TAG, "update(): " + uri  + ", count: " + count);
        return count;
    }

    /**
     * {@inheritDoc}
     *
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case BINARIES:
        	LinkedList<String> idList = new LinkedList<String>();
        	Cursor c = query(BinarySQLFormat.CONTENT_URI, new String[] { BinarySQLFormat._ID }, selection, selectionArgs, null);
        	if(c.moveToFirst()) {
        		while(!c.isAfterLast()) {
        			String id = c.getString(c.getColumnIndex(BinarySQLFormat._ID));
        			idList.add(id);
        			c.moveToNext();
        		}
        	}
        	if(c != null)
        		c.close();
        	
            count = db.delete(BINARY_TABLE_NAME, selection, selectionArgs);
            
            for(String id : idList) {
            	deleteFile(id);
            }
            break;
        case BINARY_ID:
            String binaryId = uri.getPathSegments().get(1); 
            count = db.delete(BINARY_TABLE_NAME, 
            		BinarySQLFormat._ID + "=" + binaryId + 
            		(!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), 
            		selectionArgs);
            deleteFile(uri);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        Log.i(TAG, "delete(): " + uri  + ", count: " + count);
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != BINARIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues values;
        if(initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        // REQUIRED
        if(values.containsKey(BinarySQLFormat.BINARY_CONTENT_TYPE) == false) {
            values.put(BinarySQLFormat.BINARY_CONTENT_TYPE,  "application/octet-stream");
            // use default application/octet-stream
        	//throw new SQLException("Column " + BinarySQLFormat.BINARY_CONTENT_TYPE + " is required.");
        }

        Long now = Long.valueOf(System.currentTimeMillis());        
        if(values.containsKey(BinarySQLFormat.CREATED_DATE) == false) {
            values.put(BinarySQLFormat.CREATED_DATE, now);
        }
        
        if(values.containsKey(BinarySQLFormat.MODIFIED_DATE) == false) {
            values.put(BinarySQLFormat.MODIFIED_DATE, now);
        }
        
        if(values.containsKey(BinarySQLFormat.DATA) == false) {
            values.put(BinarySQLFormat.DATA, "");
        }
 
        if(values.containsKey(BinarySQLFormat.ENCOUNTER_GUID) == false) {
            values.put(BinarySQLFormat.ENCOUNTER_GUID, "");
        }
        
        if(values.containsKey(BinarySQLFormat.ELEMENT_ID) == false) {
            values.put(BinarySQLFormat.ELEMENT_ID, "");
        }
        
        if(values.containsKey(BinarySQLFormat.BINARY_VALID) == false) {
            values.put(BinarySQLFormat.BINARY_VALID, false);
        }
        
        if(values.containsKey(BinarySQLFormat.UPLOAD_PROGRESS) == false) {
            values.put(BinarySQLFormat.UPLOAD_PROGRESS, 0);
        }
        
        if(values.containsKey(BinarySQLFormat.UPLOADED) == false) {
            values.put(BinarySQLFormat.UPLOADED, false);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(BINARY_TABLE_NAME, BinarySQLFormat.ENCOUNTER_GUID, values);

        Log.i(TAG, "insert(): " + uri  + ", rowId: " + rowId);
        
        if(rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(BinarySQLFormat.CONTENT_URI, rowId);
            //String filename = getOrCreateFile(noteUri);
            try {
                getContext().openFileOutput(filename, Context.MODE_PRIVATE).close();
                Log.i(TAG, "File path is : " + filename);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Couldn't make the file: " + e);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't make the file: " + e);
            }
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public String getType(Uri uri) {
        Log.i(TAG, "getType(uri="+uri.toString()+")");
        switch(sUriMatcher.match(uri)) {
        case BINARIES:
            return BinarySQLFormat.CONTENT_TYPE;
        case BINARY_ID:
            return BinarySQLFormat.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    public static void onCreateDatabase(SQLiteDatabase db) {
        Log.i(TAG, "Creating Binary Table");
        db.execSQL("CREATE TABLE " + BINARY_TABLE_NAME + " ("
                + BinarySQLFormat._ID + " INTEGER PRIMARY KEY,"
                + BinarySQLFormat.ENCOUNTER_GUID + " TEXT,"
                + BinarySQLFormat.ELEMENT_ID + " TEXT,"
                + BinarySQLFormat.BINARY_VALID + " INTEGER,"
                + BinarySQLFormat.BINARY_CONTENT_TYPE + " TEXT,"
                + BinarySQLFormat.UPLOAD_PROGRESS + " INTEGER,"
                + BinarySQLFormat.UPLOADED + " INTEGER,"
                + BinarySQLFormat.CREATED_DATE + " INTEGER,"
                + BinarySQLFormat.MODIFIED_DATE + " INTEGER,"
                + BinarySQLFormat.DATA + " TEXT"
                + ");");
    }
    
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

    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MocaDB.BINARY_AUTHORITY, "binaries", BINARIES);
        sUriMatcher.addURI(MocaDB.BINARY_AUTHORITY, "binaries/#", BINARY_ID);
                
        sBinaryProjectionMap = new HashMap<String, String>();
        sBinaryProjectionMap.put(BinarySQLFormat._ID, BinarySQLFormat._ID);
        sBinaryProjectionMap.put(BinarySQLFormat.ENCOUNTER_GUID, BinarySQLFormat.ENCOUNTER_GUID);
        sBinaryProjectionMap.put(BinarySQLFormat.ELEMENT_ID, BinarySQLFormat.ELEMENT_ID);
        sBinaryProjectionMap.put(BinarySQLFormat.BINARY_VALID, BinarySQLFormat.BINARY_VALID);
        sBinaryProjectionMap.put(BinarySQLFormat.BINARY_CONTENT_TYPE, BinarySQLFormat.BINARY_CONTENT_TYPE);
        sBinaryProjectionMap.put(BinarySQLFormat.UPLOAD_PROGRESS, BinarySQLFormat.UPLOAD_PROGRESS);
        sBinaryProjectionMap.put(BinarySQLFormat.UPLOADED, BinarySQLFormat.UPLOADED);
        sBinaryProjectionMap.put(BinarySQLFormat.CREATED_DATE, BinarySQLFormat.CREATED_DATE);
        sBinaryProjectionMap.put(BinarySQLFormat.MODIFIED_DATE, BinarySQLFormat.MODIFIED_DATE);
        sBinaryProjectionMap.put(BinarySQLFormat.MODIFIED_DATE, BinarySQLFormat.DATA);
    }
	
	
	
}
*/