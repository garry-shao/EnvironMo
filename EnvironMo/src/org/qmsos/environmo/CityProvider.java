package org.qmsos.environmo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CityProvider extends ContentProvider {

	public static final Uri CONTENT_URI = 
			Uri.parse("content://org.qmsos.environmo.cityprovider/cities");
	
	//base columns of database
	public static final String KEY_ID = "_id";
	public static final String KEY_CITYID = "cityid";
	public static final String KEY_NAME = "name";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_CURRENT = "current";
	public static final String KEY_FORECAST = "forecast";

	private static final int CITIES = 1;
	private static final int CITY_ID = 2;
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("org.qmsos.environmo.cityprovider", "cities", CITIES);
		uriMatcher.addURI("org.qmsos.environmo.cityprovider", "cities/#", CITY_ID);
	}

	private CityDatabaseHelper dbHelper ;
	
	@Override
	public boolean onCreate() {
		dbHelper = new CityDatabaseHelper(getContext(), 
				CityDatabaseHelper.DATABASE_NAME, null, CityDatabaseHelper.DATABASE_VERSION);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(CityDatabaseHelper.CITY_TABLE);
		
		switch (uriMatcher.match(uri)) {
		case CITY_ID:
			if (uri.getPathSegments().size() > 1) {
				queryBuilder.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
			}
			break;
		default:
			break;
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_CITYID;
		} else {
			orderBy = sortOrder;
		}
		
		Cursor cursor = queryBuilder.query(
				database, projection, selection, selectionArgs, null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CITIES:
			return "vnd.android.cursor.dir/vnd.org.qmsos.environmo";
		case CITY_ID:
			return "vnd.android.cursor.item/vnd.org.qmsos.environmo";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		long rowID = database.insert(CityDatabaseHelper.CITY_TABLE, "city", values);
		if (rowID > 0) {
			Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			
			getContext().getContentResolver().notifyChange(resultUri, null);
			
			return resultUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		int count;
		switch (uriMatcher.match(uri)) {
		case CITIES:
			count = database.delete(CityDatabaseHelper.CITY_TABLE, selection, selectionArgs);
			
			break;
		case CITY_ID:
			String where = KEY_ID + "=" + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.delete(CityDatabaseHelper.CITY_TABLE, where, selectionArgs);
			
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		int count;
		switch (uriMatcher.match(uri)) {
		case CITIES:
			count = database.update(CityDatabaseHelper.CITY_TABLE, values, selection, selectionArgs);
			
			break;
		case CITY_ID:
			String where = KEY_ID + "=" + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.update(CityDatabaseHelper.CITY_TABLE, values, where, selectionArgs);
			
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	private static class CityDatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = CityProvider.class.getSimpleName();
		
		private static final String DATABASE_NAME = "cities.db";
		private static final int DATABASE_VERSION = 1;
		private static final String CITY_TABLE = "cities";
		
		private static final String DATABASE_CREATE = 
				"CREATE TABLE " + CITY_TABLE + " (" + 
						KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						KEY_CITYID + " INTEGER, " +
						KEY_NAME + " TEXT, " + 
						KEY_COUNTRY + " TEXT, " + 
						KEY_LONGITUDE + " REAL, " +
						KEY_LATITUDE + " REAL, " +
						KEY_CURRENT + " TEXT, " + 
						KEY_FORECAST + " TEXT);"; 
	
		public CityDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + 
					newVersion + ", which will destroy all old data");
			
			db.execSQL("DROP TABLE IF EXISTS " + CITY_TABLE);
			onCreate(db);
		}
	}

}
