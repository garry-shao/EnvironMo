package org.qmsos.weathermo.contract;

import android.net.Uri;

/**
 * The contract of the weather provider. Contains definition of the supported 
 * URIs and data columns.
 */
public final class ProviderContract {

    /**
     * Authority used in CRUD operations from weather provider.
     */
    public static final String AUTHORITY = "org.qmsos.weathermo.weatherprovider";

    /**
     * Class that represents entity of the cities table.
     */
    public static final class CityEntity implements BaseColumns, CityColumns {

        /**
         * The URL for accessing cities(content://).
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cities");
    }

    /**
     * Class that represents entity of the weather table.
     */
    public static final class WeatherEntity implements BaseColumns, WeatherColumns {

        /**
         * The URL for accessing weather(content://).
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/weather");
    }

    /**
     * Base columns that both tables contains.
     */
    protected interface BaseColumns {

        /**
         * The id that representing the city. <p>TYPE: INTEGER</p>
         */
        String CITY_ID = "cityId";
    }

    /**
     * Columns of the cities table.
     */
    protected interface CityColumns {
        /**
         * Primary key that auto increments. <p>TYPE: INTEGER</p>
         */
        String INDEX = "_id";

        /**
         * The name of the city. <p>TYPE: TEXT</p>
         */
        String CITY_NAME = "cityName";

        /**
         * The abbreviation code of the city's country. <p>TYPE: TEXT</p>
         */
        String COUNTRY = "country";

        /**
         * The longitude of the city, negative for southern latitude. <p>TYPE: REAL</p>
         */
        String LONGITUDE = "longitude";

        /**
         * The latitude of the city, negative for southern latitude. <p>TYPE: REAL</p>
         */
        String LATITUDE = "latitude";
    }

    /**
     * Columns of the weather table.
     */
    protected interface WeatherColumns {
        /**
         * The parsed string of the current weather, this contains multiple
         * values aggregated as single instance of string. <p>TYPE: TEXT</p>
         */
        String CURRENT = "current";

        /**
         * Current ultra-violet radiation index. <p>TYPE: REAL</p>
         */
        String UV_INDEX = "uvIndex";

        /**
         * The parsed string of the forecast weather <b>day 1</b>, this contains multiple
         * values aggregated as single instance of string. <p>TYPE: TEXT</p>
         */
        String FORECAST1 = "forecast1";

        /**
         * The parsed string of the forecast weather <b>day 2</b>, this contains multiple
         * values aggregated as single instance of string. <p>TYPE: TEXT</p>
         */
        String FORECAST2 = "forecast2";

        /**
         * The parsed string of the forecast weather <b>day 3</b>, this contains multiple
         * values aggregated as single instance of string. <p>TYPE: TEXT</p>
         */
        String FORECAST3 = "forecast3";
    }
}