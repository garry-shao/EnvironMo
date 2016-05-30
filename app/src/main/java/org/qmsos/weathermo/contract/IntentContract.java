package org.qmsos.weathermo.contract;

/**
 * The contract of the intents in application scope. Contains definition of intent
 * action and intent's extra key.
 *
 */
public final class IntentContract {

	// base package name, normally app's package name.
	private static final String BASE_PACKAGE_NAME = "org.qmsos.weathermo" + ".";
	
	// below are standard intent actions.
	
	/**
	 * Broadcast action: setting up repeat alarm.
	 */
	public static final String ACTION_REFRESH_ALARM = BASE_PACKAGE_NAME + "ACTION_REFRESH_ALARM";
	
	/**
	 * Service action: refreshing weather automatically.
	 */
	public static final String ACTION_REFRESH_WEATHER_AUTO = BASE_PACKAGE_NAME + "ACTION_REFRESH_WEATHER_AUTO";
	
	/**
	 * Used as a boolean field in determine whether start the automatic refreshing.
	 */
	public static final String EXTRA_REFRESH_WEATHER_AUTO = BASE_PACKAGE_NAME + "EXTRA_REFRESH_WEATHER_AUTO";
	
	/**
	 * Activity action: refreshing weather manually.
	 */
	public static final String ACTION_REFRESH_WEATHER_MANUAL = BASE_PACKAGE_NAME + "ACTION_REFRESH_WEATHER_MANUAL";
	
	/**
	 * Activity action: insert city.
	 */
	public static final String ACTION_INSERT_CITY = BASE_PACKAGE_NAME + "ACTION_INSERT_CITY";
	
	/**
	 * Used as a parcel key in containing the city that will be inserted.
	 */
	public static final String EXTRA_INSERT_CITY = BASE_PACKAGE_NAME + "EXTRA_INSERT_CITY";

	/**
	 * Service action: action of inserting city executed, normally used in 
	 * {@linkplain android.support.v4.content.LocalBroadcastManager LocalBroadcastManager}, 
	 * this should be seen as reporting back of {@link #ACTION_INSERT_CITY}.
	 */
	public static final String ACTION_INSERT_EXECUTED = BASE_PACKAGE_NAME + "ACTION_INSERT_EXECUTED";
	
	/**
	 * Used as a boolean field in determine whether the action of inserting city succeeded.
	 */
	public static final String EXTRA_INSERT_EXECUTED = BASE_PACKAGE_NAME + "EXTRA_INSERT_EXECUTED";
	
	/**
	 * Activity action: delete city.
	 */
	public static final String ACTION_DELETE_CITY = BASE_PACKAGE_NAME + "ACTION_DELETE_CITY";

	/**
	 * Used as an integer field containing the city id.
	 */
	public static final String EXTRA_CITY_ID = BASE_PACKAGE_NAME + "EXTRA_CITY_ID";

	/**
	 * Activity action: search for city.
	 */
	public static final String ACTION_SEARCH_CITY = BASE_PACKAGE_NAME + "ACTION_SEARCH_CITY";

	/**
	 * Used as a string field containing the name string that to be searched.
	 */
	public static final String EXTRA_CITY_NAME = BASE_PACKAGE_NAME + "EXTRA_CITY_NAME";

	/**
	 * Service action: action of searching city executed, normally used in 
	 * {@linkplain android.support.v4.content.LocalBroadcastManager LocalBroadcastManager}, 
	 * this should be seen as reporting back of {@link #ACTION_SEARCH_CITY}.
	 */
	public static final String ACTION_SEARCH_EXECUTED = BASE_PACKAGE_NAME + "ACTION_SEARCH_EXECUTED";

	/**
	 * Used as a boolean field in determine whether the action of searching city succeeded.
	 */
	public static final String EXTRA_SEARCH_EXECUTED = BASE_PACKAGE_NAME + "EXTRA_SEARCH_EXECUTED";

	/**
	 * Used as a string field containing the result of a succeeded search action.
	 */
	public static final String EXTRA_SEARCH_RESULT = BASE_PACKAGE_NAME + "EXTRA_QUERY_RESULT";

}
