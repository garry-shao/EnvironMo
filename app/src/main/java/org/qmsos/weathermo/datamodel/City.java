package org.qmsos.weathermo.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description of city instance.
 */
public class City implements Parcelable {

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
		return "id=" + mCityId +
				",name=" + mCityName +
				",country=" + mCountry +
				",Longitude:" + mLongitude +
				",Latitude:" + mLatitude;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mCityId);
		dest.writeString(mCityName);
		dest.writeString(mCountry);
		dest.writeDouble(mLongitude);
		dest.writeDouble(mLatitude);
	}
	
	public static final Creator<City> CREATOR = new Creator<City>() {

		@Override
		public City createFromParcel(Parcel source) {
			return new City(source);
		}

		@Override
		public City[] newArray(int size) {
			return new City[size];
		}
	};

	private City(Parcel parcel) {
		mCityId = parcel.readLong();
		mCityName = parcel.readString();
		mCountry = parcel.readString();
		mLongitude = parcel.readDouble();
		mLatitude = parcel.readDouble();
	}

}