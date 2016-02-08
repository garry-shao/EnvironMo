package org.qmsos.weathermo.util;

public class City {

	private final long mCityId;
	private final String mCityName;
	private final String mCountry;
	private final double mLongitude;
	private final double mLatitude;

	public City(long cityId, String name, String country, double longitude, double latitude) {
		this.mCityId = cityId;
		this.mCityName = name;
		this.mCountry = country;
		this.mLongitude = longitude;
		this.mLatitude = latitude;
	}

	public long getCityId() {
		return mCityId;
	}

	public String getCityName() {
		return mCityName;
	}

	public String getCountry() {
		return mCountry;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	@Override
	public String toString() {
		return "id=" + mCityId + ",name=" + mCityName 
				+ ",country=" + mCountry + ",Longitude:" + mLongitude + ",Latitude:" + mLatitude;
	}
	
}