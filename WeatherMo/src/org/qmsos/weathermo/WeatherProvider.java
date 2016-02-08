package org.qmsos.weathermo;

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

public class WeatherProvider extends ContentProvider {

	public static final Uri CONTENT_URI_CITIES = 
			Uri.parse("content://org.qmsos.weathermo.weatherprovider/cities");
	public static final Uri CONTENT_URI_WEATHER = 
			Uri.parse("content://org.qmsos.weathermo.weatherprovider/weather");
	
	// base columns of database
	
	// cities columns
	public static final String KEY_ID = "_id";
	public static final String KEY_CITY_ID = "city_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	
	// weather columns
	public static final String KEY_CURRENT = "current";
	public static final String KEY_FORECAST = "forecast";

	private static final int CITIES = 1;
	private static final int CITY_ID = 2;
	private static final int WEATHER = 3;
	private static final int WEATHER_ID = 4;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		
		URI_MATCHER.addURI("org.qmsos.weathermo.weatherprovider", "cities", CITIES);
		URI_MATCHER.addURI("org.qmsos.weathermo.weatherprovider", "cities/#", CITY_ID);
		URI_MATCHER.addURI("org.qmsos.weathermo.weatherprovider", "weather", WEATHER);
		URI_MATCHER.addURI("org.qmsos.weathermo.weatherprovider", "weather/#", WEATHER_ID);
	}

	private DatabaseHelper mDatabaseHelper;
	
	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext(), 
				DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.DATABASE_VERSION);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
		case CITY_ID:
			queryBuilder.setTables(DatabaseHelper.TABLE_CITIES);
			if (uri.getPathSegments().size() > 1) {
				queryBuilder.appendWhere(KEY_ID + " = " + uri.getPathSegments().get(1));
			}
			break;
		case WEATHER:
		case WEATHER_ID:
			queryBuilder.setTables(DatabaseHelper.TABLE_WEATHER);
			if (uri.getPathSegments().size() > 1) {
				queryBuilder.appendWhere(KEY_ID + " = " + uri.getPathSegments().get(1));
			}
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_CITY_ID;
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
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
		case WEATHER:
			return "vnd.android.cursor.dir/vnd.org.qmsos.weathermo";
		case CITY_ID:
		case WEATHER_ID:
			return "vnd.android.cursor.item/vnd.org.qmsos.weathermo";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
		case CITY_ID:
			long rowID = database.insert(DatabaseHelper.TABLE_CITIES, "city", values);
			if (rowID > 0) {
				Uri resultUri = ContentUris.withAppendedId(CONTENT_URI_CITIES, rowID);
				
				getContext().getContentResolver().notifyChange(resultUri, null);
				
				return resultUri;
			}
		case WEATHER:
		case WEATHER_ID:
			rowID = database.insert(DatabaseHelper.TABLE_WEATHER, "weather", values);
			if (rowID > 0) {
				Uri resultUri = ContentUris.withAppendedId(CONTENT_URI_WEATHER, rowID);
				
				getContext().getContentResolver().notifyChange(resultUri, null);
				
				return resultUri;
			}
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
		case CITY_ID:
			database.beginTransaction();
			try {
				for (ContentValues value : values) {
					long rowID = database.insert(DatabaseHelper.TABLE_CITIES, "city", value);
					if (rowID < 0) {
						return 0;
					}
				}
				database.setTransactionSuccessful();
			} finally {
				database.endTransaction();
			}
			
			getContext().getContentResolver().notifyChange(uri, null);
			
			return values.length;
		case WEATHER:
		case WEATHER_ID:
			database.beginTransaction();
			try {
				for (ContentValues value : values) {
					long rowID = database.insert(DatabaseHelper.TABLE_WEATHER, "weather", value);
					if (rowID < 0) {
						return 0;
					}
				}
				database.setTransactionSuccessful();
			} finally {
				database.endTransaction();
			}
			
			getContext().getContentResolver().notifyChange(uri, null);
			
			return values.length;
		default:
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		int count;
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
			count = database.delete(DatabaseHelper.TABLE_CITIES, selection, selectionArgs);
			
			break;
		case CITY_ID:
			String where = KEY_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.delete(DatabaseHelper.TABLE_CITIES, where, selectionArgs);
			
			break;
		case WEATHER:
			count = database.delete(DatabaseHelper.TABLE_WEATHER, selection, selectionArgs);
			
			break;
		case WEATHER_ID:
			where = KEY_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.delete(DatabaseHelper.TABLE_WEATHER, where, selectionArgs);
	
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		int count;
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
			count = database.update(DatabaseHelper.TABLE_CITIES, values, selection, selectionArgs);
			
			break;
		case CITY_ID:
			String where = KEY_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.update(DatabaseHelper.TABLE_CITIES, values, where, selectionArgs);
			
			break;
		case WEATHER:
			count = database.update(DatabaseHelper.TABLE_WEATHER, values, selection, selectionArgs);
			
			break;
		case WEATHER_ID:
			where = KEY_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.update(DatabaseHelper.TABLE_WEATHER, values, where, selectionArgs);

			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = WeatherProvider.class.getSimpleName();
		
		private static final String DATABASE_NAME = "weathers.db";
		private static final int DATABASE_VERSION = 1;
		
		private static final String TABLE_CITIES = "cities";
		private static final String TABLE_WEATHER = "weather";
		
		private static final String CREATE_TABLE_CITIES = 
				"CREATE TABLE " + TABLE_CITIES + " (" + 
						KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						KEY_CITY_ID + " INTEGER, " +
						KEY_NAME + " TEXT, " + 
						KEY_COUNTRY + " TEXT, " + 
						KEY_LONGITUDE + " REAL, " +
						KEY_LATITUDE + " REAL);"; 
		
		private static final String CREATE_TABLE_WEATHER = 
				"CREATE TABLE " + TABLE_WEATHER + " (" + 
						KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						KEY_CITY_ID + " INTEGER, " +
						KEY_CURRENT + " TEXT, " +
						KEY_FORECAST + " TEXT);";
		
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_CITIES);
			db.execSQL(CREATE_TABLE_WEATHER);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + 
					newVersion + ", which will destroy all old data");
			
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER);
			onCreate(db);
		}
	}

}
