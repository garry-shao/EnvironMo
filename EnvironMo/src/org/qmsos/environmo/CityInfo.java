package org.qmsos.environmo;

class CityInfo {

	private final int id;
	private final String name;
	private final String country;
	private final double longitude;
	private final double latitude;

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
	
}