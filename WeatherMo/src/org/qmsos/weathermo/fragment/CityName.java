package org.qmsos.weathermo.fragment;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.LoaderContract;
import org.qmsos.weathermo.contract.ProviderContract.CityEntity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class CityName extends Fragment implements LoaderCallbacks<Cursor> {

	private OnCityNameViewClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnCityNameViewClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnCityNameViewClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_city_name, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TextView cityName = (TextView) getView().findViewById(R.id.city_name);
		cityName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onCityNameViewClicked();
			}
		});
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		
		super.onDestroyView();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long cityId;
		if (args != null) {
			cityId = args.getLong(LoaderContract.KEY_CITY_ID);
		} else {
			cityId = 0L;
		}
		
		String[] projection = { CityEntity.CITY_ID, CityEntity.CITY_NAME, CityEntity.COUNTRY };
		String where = CityEntity.CITY_ID + " = " + cityId;
		
		return new CursorLoader(getContext(), CityEntity.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		String cityName = null;
		String country = null;
		if (data != null & data.moveToFirst()) {
			cityName = data.getString(data.getColumnIndexOrThrow(CityEntity.CITY_NAME));
			country = data.getString(data.getColumnIndexOrThrow(CityEntity.COUNTRY));
		}
		
		TextView textView = (TextView) getView().findViewById(R.id.city_name);
		if (cityName != null && country != null) {
			String raw = cityName + " " +country;
			
			SpannableString spanned = new SpannableString(raw);
			spanned.setSpan(new RelativeSizeSpan(0.5f), 
					cityName.length() + 1, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			textView.setText(spanned);
		} else {
			textView.setText(R.string.ui_placeholder);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Callback that will be invoked when user clicked view of city name.
	 */
	public interface OnCityNameViewClickedListener {
		
		/**
		 * Called when view of city name is clicked.
		 */
		void onCityNameViewClicked();
	}

}
