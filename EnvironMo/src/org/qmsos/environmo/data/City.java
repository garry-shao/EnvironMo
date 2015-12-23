package org.qmsos.environmo.data;

public class City {

	private final int id;
	private final double longitude;
	private final double latitude;
	private final String name;
	private final String country;

	public City(int id, String name, String country, double longitude, double latitude) {
		this.id = id;
		this.name = name;
		this.country = country;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public int getId() {
		return id;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public String getName() {
		return name;
	}

	public String getCountry() {
		return country;
	}
	
}