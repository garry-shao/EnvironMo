package org.qmsos.weathermo.util;

public class Weather {
	
	private final int weatherId;
	private final int temperature;
	
	public Weather(int weatherId, int temperature) {
		this.weatherId = weatherId;
		this.temperature = temperature;
	}

	public int getWeatherId() {
		return weatherId;
	}

	public int getTemperature() {
		return temperature;
	}

}