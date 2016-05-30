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

/**
 * A fragment used as header to show info of the city currently showing. 
 *
 */
public class CityHeader extends Fragment implements LoaderCallbacks<Cursor> {

	private OnCityHeaderClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnCityHeaderClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnCityHeaderClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_city_header, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TextView cityHeader = (TextView) getView().findViewById(R.id.current_city);
		cityHeader.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onCityHeaderClicked();
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
		String city = null;
		String country = null;
		if (data != null & data.moveToFirst()) {
			city = data.getString(data.getColumnIndexOrThrow(CityEntity.CITY_NAME));
			country = data.getString(data.getColumnIndexOrThrow(CityEntity.COUNTRY));
		}
		
		TextView tv = (TextView) getView().findViewById(R.id.current_city);
		if (city != null && country != null) {
			String raw = city + " " +country;
			
			SpannableString spanned = new SpannableString(raw);
			spanned.setSpan(new RelativeSizeSpan(0.5f), 
					city.length() + 1, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			tv.setText(spanned);
		} else {
			tv.setText(R.string.ui_placeholder);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Callback that will be invoked when user click view of city header.
	 */
	public interface OnCityHeaderClickedListener {
		
		/**
		 * Called when view of city header is clicked.
		 */
		void onCityHeaderClicked();
	}

}
