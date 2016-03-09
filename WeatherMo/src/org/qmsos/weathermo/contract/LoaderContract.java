package org.qmsos.weathermo.contract;

/**
 * The contract used in loader framework in application scope. Contains definition
 * of keys used in bundle when creating loader.
 *
 */
public final class LoaderContract {

	// base package name, normally app's package name.
	private static final String BASE_PACKAGE_NAME = "org.qmsos.weathermo" + ".";

	/**
	 * Used as key of long integer field containing city id in the bundle that 
	 * being used in loader framework.
	 */
	public static final String KEY_CITY_ID = BASE_PACKAGE_NAME + "KEY_CITY_ID";

	/**
	 * Used as key of integer field containing which day to load in the bundle 
	 * that being used in loader framework.
	 */
	public static final String KEY_DAY = BASE_PACKAGE_NAME + "KEY_DAY";

}
