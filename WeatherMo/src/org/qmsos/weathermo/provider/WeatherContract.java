package org.qmsos.weathermo.provider;

import android.net.Uri;

public final class WeatherContract {

	public static final String AUTHORITY = "org.qmsos.weathermo.weatherprovider";
	
	public static final class CityEntity implements BaseColumns, CityColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cities");
	}
	
	public static final class WeatherEntity implements BaseColumns, WeatherColunms {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/weather");
	}
	
	protected interface BaseColumns {
		public static final String CITY_ID = "cityId";
	}
	
	protected interface CityColumns {
		public static final String INDEX = "_id";
		public static final String CITY_NAME = "cityName";
		public static final String COUNTRY = "country";
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
	}
	
	protected interface WeatherColunms {
		public static final String CURRENT = "current";
		public static final String UV_INDEX = "uvIndex";
		public static final String FORECAST1 = "forecast1";
		public static final String FORECAST2 = "forecast2";
		public static final String FORECAST3 = "forecast3";
	}

}
