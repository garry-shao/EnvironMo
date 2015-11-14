package org.qmsos.environmo;

class CityInfo {

//	Constants for a specific city.
	private final int id;
	private final String name;
	private final String country;
	private final double longitude;
	private final double latitude;

//	Variants for a specific city.
	private long sunrise;
	private long sunset;
	
	public CityInfo(int id, String name, String country, double longitude, double latitude) {
		this.id = id;
		this.name = name;
		this.country = country;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public int getId() {
		return id;
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

	public long getSunrise() {
		return sunrise;
	}

	public void setSunrise(long sunrise) {
		this.sunrise = sunrise;
	}

	public long getSunset() {
		return sunset;
	}

	public void setSunset(long sunset) {
		this.sunset = sunset;
	}
	
}