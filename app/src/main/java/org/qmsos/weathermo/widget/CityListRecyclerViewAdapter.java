package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.ProviderContract.CityEntity;

import android.content.Context;
import android.database.Cursor;
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
 * Implementation that shows city list and a type at tails of this list.
 */
public class CityListRecyclerViewAdapter extends BaseDualRecyclerViewAdapter<ViewHolder> {

	private OnDeleteCityClickedListener mListener;
	
	public CityListRecyclerViewAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		
		try {
			mListener = (OnDeleteCityClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnDeleteCityClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public void onBindViewHolderMain(ViewHolder holder, Cursor cursor) {
		final long cityId = cursor.getLong(cursor.getColumnIndexOrThrow(CityEntity.CITY_ID));
		String cityName = cursor.getString(cursor.getColumnIndexOrThrow(CityEntity.CITY_NAME));
		String country = cursor.getString(cursor.getColumnIndexOrThrow(CityEntity.COUNTRY));
		double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(CityEntity.LONGITUDE));
		double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(CityEntity.LATITUDE));
		
		int maxLengthOfCityName = 15;
		if (cityName.length() > maxLengthOfCityName) {
			cityName = cityName.substring(0, maxLengthOfCityName) + "...";
		}
		String raw = cityName + " " + country;
		
		SpannableString spanned = new SpannableString(raw);
		spanned.setSpan(new RelativeSizeSpan(0.5f), 
				cityName.length() + 1, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		String lon = longitude > 0 ? Math.abs(longitude) + "\u00b0E" : Math.abs(longitude) + "\u00b0W";
		String lat = latitude > 0 ? Math.abs(latitude) + "\u00b0N" : Math.abs(latitude) + "\u00b0S";
		
		((MainViewHolder) holder).mCityInfoView.setText(spanned);
		((MainViewHolder) holder).mCityGeoView.setText(lon + " " + lat);
		((MainViewHolder) holder).mDeleteButton.setText(R.string.button_delete);
		((MainViewHolder) holder).mDeleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onDeleteCityClicked(cityId);
			}
		});
	}

	@Override
	public void onBindViewHolderSub(ViewHolder holder) {
		((SubViewHolder) holder).mMoreButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onSubViewButtonClicked();
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
		case VIEW_TYPE_MAIN:
			View view = View.inflate(parent.getContext(), R.layout.view_holder_city, null);
			
			return new MainViewHolder(view);
		case VIEW_TYPE_SUB:
			View addView = View.inflate(parent.getContext(), R.layout.view_holder_extra, null);
			
			return new SubViewHolder(addView);
		default:
			return null;
		}
	}

	/**
	 * Implementation of ViewHolder that will be displayed on the main.
	 */
	static class MainViewHolder extends ViewHolder {
		TextView mCityInfoView;
		TextView mCityGeoView;
		Button mDeleteButton;

		public MainViewHolder(View itemView) {
			super(itemView);

			mCityInfoView = (TextView) itemView.findViewById(R.id.city_main);
			mCityGeoView = (TextView) itemView.findViewById(R.id.city_sub);
			mDeleteButton = (Button) itemView.findViewById(R.id.city_button);
		}
	
	}
	
	/**
	 * Implementation of ViewHolder that will be displayed at the tail.
	 */
	static class SubViewHolder extends ViewHolder {
		Button mMoreButton;
		
		public SubViewHolder(View itemView) {
			super(itemView);
			
			mMoreButton = (Button) itemView.findViewById(R.id.city_more);
		}
	
	}

	/**
	 * Interface of the callback to be invoked when the buttons that inside 
	 * of ViewHolder are clicked.
	 */
	public interface OnDeleteCityClickedListener {
		/**
		 * Callback when to button at the tail of view is clicked.
		 */
		void onSubViewButtonClicked();
		/**
		 * Callback to delete city.
		 * 
		 * @param cityId
		 *            The id of the city to delete.
		 */
		void onDeleteCityClicked(long cityId);
	}

}
