package org.qmsos.weathermo.util;

public class City {

	private final long cityId;
	private final String name;
	private final String country;
	private final double longitude;
	private final double latitude;

	public City(long cityId, String name, String country, double longitude, double latitude) {
		this.cityId = cityId;
		this.name = name;
		this.country = country;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public long getCityId() {
		return cityId;
	}

	public String getName() {
		return name;
	}

	public String getCountry() {
		return country;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	@Override
	public String toString() {
		return "id=" + cityId + ",name=" + name 
				+ ",country=" + country + ",Longitude:" + longitude + ",Latitude:" + latitude;
	}
	
}