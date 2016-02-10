package org.qmsos.weathermo.util;

public class IpcConstants {

	private static final String BASE_PACKAGE_NAME = "org.qmsos.weathermo" + ".";
	
	public static final String ACTION_REFRESH_WEATHER = BASE_PACKAGE_NAME + "ACTION_REFRESH_WEATHER";
	
	public static final String ACTION_DELETE_CITY = BASE_PACKAGE_NAME +"ACTION_DELETE_CITY";
	public static final String EXTRA_CITY_ID = "EXTRA_CITY_ID";
	
	public static final String ACTION_QUERY_CITY = BASE_PACKAGE_NAME + "ACTION_QUERY_CITY";
	public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";

	public static final String ACTION_QUERY_EXECUTED = BASE_PACKAGE_NAME + "ACTION_QUERY_EXECUTED";
	public static final String EXTRA_QUERY_EXECUTED = "EXTRA_QUERY_EXECUTED";
	public static final String EXTRA_QUERY_RESULT = "EXTRA_QUERY_RESULT";

	public static final String ACTION_ADD_CITY = BASE_PACKAGE_NAME + "ACTION_ADD_CITY";
	public static final String EXTRA_ADD_CITY = BASE_PACKAGE_NAME + "EXTRA_ADD_CITY";

	public static final String ACTION_ADD_EXECUTED = BASE_PACKAGE_NAME + "ACTION_ADD_EXECUTED";
	public static final String EXTRA_ADD_EXECUTED = "EXTRA_ADD_EXECUTED";

}
