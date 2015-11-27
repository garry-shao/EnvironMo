package org.qmsos.environmo;

class WeatherInfo {
	
	private final String weatherMain;
	private final String weatherDescription;

	private int temperature;
	private int temperatureMin;
	private int temperatureMax;

	private int pressure;
	private int humidity;
	
	private int windSpeed;
	private int windDirection;
	
	public WeatherInfo(String weatherMain, String weatherDescription) {
		this.weatherMain = weatherMain;
		this.weatherDescription = weatherDescription;
	}

	public String getWeatherMain() {
		return weatherMain;
	}

	public String getWeatherDescription() {
		return weatherDescription;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getTemperatureMin() {
		return temperatureMin;
	}

	public void setTemperatureMin(int temperatureMin) {
		this.temperatureMin = temperatureMin;
	}

	public int getTemperatureMax() {
		return temperatureMax;
	}

	public void setTemperatureMax(int temperatureMax) {
		this.temperatureMax = temperatureMax;
	}

	public int getPressure() {
		return pressure;
	}

	public void setPressure(int pressure) {
		this.pressure = pressure;
	}

	public int getHumidity() {
		return humidity;
	}

	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	public int getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
	}

	public int getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(int windDirection) {
		this.windDirection = windDirection;
	}
	
}