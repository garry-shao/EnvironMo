package org.qmsos.weathermo.model;

/**
 * Description of weather instance.
 */
public class Weather {

    private final int mWeatherId;
    private final int mTemperature;

    public Weather(int weatherId, int temperature) {
        this.mWeatherId = weatherId;
        this.mTemperature = temperature;
    }

    public int getWeatherId() {
        return mWeatherId;
    }

    public int getTemperature() {
        return mTemperature;
    }
}