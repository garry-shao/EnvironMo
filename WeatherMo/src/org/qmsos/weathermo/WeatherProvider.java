package org.qmsos.weathermo;

import java.util.ArrayList;

import org.qmsos.weathermo.provider.WeatherContract.CityEntity;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
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

	private static final int CITIES = 1;
	private static final int WEATHER = 2;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		
		URI_MATCHER.addURI("org.qmsos.weathermo.weatherprovider", "cities", CITIES);
		URI_MATCHER.addURI("org.qmsos.weathermo.weatherprovider", "weather", WEATHER);
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
			queryBuilder.setTables(DatabaseHelper.TABLE_CITIES);
			
			break;
		case WEATHER:
			queryBuilder.setTables(DatabaseHelper.TABLE_WEATHER);

			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = CityEntity.CITY_ID;
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
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
			long rowId = database.insert(DatabaseHelper.TABLE_CITIES, "city", values);
			if (rowId > 0) {
				Uri resultUri = ContentUris.withAppendedId(CityEntity.CONTENT_URI, rowId);
				
				getContext().getContentResolver().notifyChange(resultUri, null);
				
				return resultUri;
			}
		case WEATHER:
			rowId = database.insert(DatabaseHelper.TABLE_WEATHER, "weather", values);
			if (rowId > 0) {
				Uri resultUri = ContentUris.withAppendedId(WeatherEntity.CONTENT_URI, rowId);
				
				getContext().getContentResolver().notifyChange(resultUri, null);
				
				return resultUri;
			}
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		int count;
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
			count = database.delete(DatabaseHelper.TABLE_CITIES, selection, selectionArgs);
			
			break;
		case WEATHER:
			count = database.delete(DatabaseHelper.TABLE_WEATHER, selection, selectionArgs);
			
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
		case WEATHER:
			count = database.update(DatabaseHelper.TABLE_WEATHER, values, selection, selectionArgs);
			
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		database.beginTransaction();
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			database.setTransactionSuccessful();
			
			return results;
		} finally {
			database.endTransaction();
		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		switch (URI_MATCHER.match(uri)) {
		case CITIES:
			database.beginTransaction();
			try {
				for (ContentValues value : values) {
					long rowId = database.insert(DatabaseHelper.TABLE_CITIES, "city", value);
					if (rowId < 0) {
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
			database.beginTransaction();
			try {
				for (ContentValues value : values) {
					long rowId = database.insert(DatabaseHelper.TABLE_WEATHER, "weather", value);
					if (rowId < 0) {
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

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = DatabaseHelper.class.getSimpleName();
		
		private static final String DATABASE_NAME = "weathers.db";
		private static final int DATABASE_VERSION = 1;
		
		private static final String TABLE_CITIES = "cities";
		private static final String TABLE_WEATHER = "weather";
		
		private static final String CREATE_TABLE_CITIES = 
				"CREATE TABLE " + TABLE_CITIES + " (" + 
						CityEntity.INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						CityEntity.CITY_ID + " INTEGER, " +
						CityEntity.CITY_NAME + " TEXT, " + 
						CityEntity.COUNTRY + " TEXT, " + 
						CityEntity.LONGITUDE + " REAL, " +
						CityEntity.LATITUDE + " REAL);"; 
		
		private static final String CREATE_TABLE_WEATHER = 
				"CREATE TABLE " + TABLE_WEATHER + " (" + 
						WeatherEntity.CITY_ID + " INTEGER, " +
						WeatherEntity.CURRENT + " TEXT, " + 
						WeatherEntity.UV_INDEX + " REAL, " + 
						WeatherEntity.FORECAST1 + " TEXT, " + 
						WeatherEntity.FORECAST2 + " TEXT, " + 
						WeatherEntity.FORECAST3 + " TEXT);"; 
		
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
