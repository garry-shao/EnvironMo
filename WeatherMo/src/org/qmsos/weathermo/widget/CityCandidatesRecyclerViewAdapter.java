package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.datamodel.City;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Implementation that shows a list of candidate cities from data that passed.
 */
public class CityCandidatesRecyclerViewAdapter extends BaseArrayRecyclerViewAdapter<City, ViewHolder> {

	private OnInsertCityClickedListener mListener;

	public CityCandidatesRecyclerViewAdapter(Context context, City[] dataArray) {
		super(context, dataArray);
		
		try {
			mListener = (OnInsertCityClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnInsertCityClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, City data) {
		final City fData = data;
		
		int maxLengthOfCityName = 15;
		String cityName = data.getCityName();
		if (cityName.length() > maxLengthOfCityName) {
			cityName = cityName.substring(0, maxLengthOfCityName) + "...";
		}
		String country = data.getCountry();
		String raw = cityName + " " + country;
		
		SpannableString spanned = new SpannableString(raw);
		spanned.setSpan(new RelativeSizeSpan(0.5f), 
				cityName.length() + 1, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		double longitude = data.getLongitude();
		double latitude = data.getLatitude();
		String lon = longitude > 0 ? Math.abs(longitude) + "\u00b0E" : Math.abs(longitude) + "\u00b0W";
		String lat = latitude > 0 ? Math.abs(latitude) + "\u00b0N" : Math.abs(latitude) + "\u00b0S";
		
		((RecyclerViewHolder) holder).mCityInfoView.setText(spanned);
		((RecyclerViewHolder) holder).mCityGeoView.setText(lon + " " + lat);
		((RecyclerViewHolder) holder).mInsertButton.setText(R.string.button_insert);
		((RecyclerViewHolder) holder).mInsertButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onInsertCityClicked(fData);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder_city, null);
		
		return new RecyclerViewHolder(view);
	}

	/**
	 * Implementation of ViewHolder that will be displayed. 
	 */
	static class RecyclerViewHolder extends ViewHolder {
		TextView mCityInfoView;
		TextView mCityGeoView;
		Button mInsertButton;
		
		public RecyclerViewHolder(View itemView) {
			super(itemView);
			
			mCityInfoView = (TextView) itemView.findViewById(R.id.city_main);
			mCityGeoView = (TextView) itemView.findViewById(R.id.city_sub);
			mInsertButton = (Button) itemView.findViewById(R.id.city_button);
		}
	
	}

	/**
	 * Interface of the callback to be invoked when the button of adding city
	 * that inside of ViewHolder is clicked.
	 */
	public interface OnInsertCityClickedListener {
		/**
		 * Callback to insert city.
		 * 
		 * @param city
		 *            The city to insert.
		 */
		void onInsertCityClicked(City city);
	}

}
