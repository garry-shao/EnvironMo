package org.qmsos.weathermo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.datamodel.City;
import org.qmsos.weathermo.util.CityParser;
import org.qmsos.weathermo.widget.CitySearchRecyclerViewAdapter;

/**
 * Fragment that provides framework to search remote server for valid city.
 */
public class CitySearch extends Fragment {

	private static final String KEY_CITY_SEARCH = "KEY_CITY_SEARCH";

	private OnStartSearchListener mListener;
	
	private CitySearchRecyclerViewAdapter mCitySearchAdapter;

	private City[] mResults = null;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnStartSearchListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnStartSearchListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_city_search, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (savedInstanceState != null) {
			mResults = (City[]) savedInstanceState.getParcelableArray(KEY_CITY_SEARCH);
		}
		
		mCitySearchAdapter = new CitySearchRecyclerViewAdapter(getContext(), mResults);
		RecyclerView rv = (RecyclerView) view.findViewById(R.id.city_results);
		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(mCitySearchAdapter);
		
		EditText cityNameInput = (EditText) view.findViewById(R.id.city_name_input);
		cityNameInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String cityName = v.getText().toString();
					mListener.onStartSearch(cityName);
					
					InputMethodManager manager = (InputMethodManager)
                            getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					
					return true;
				} else {
					return false;
				}
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArray(KEY_CITY_SEARCH, mResults);
		
		super.onSaveInstanceState(outState);
	}

	public void swapData(String result) {
		mResults = CityParser.parseResult(result);
		
		mCitySearchAdapter.swapData(mResults);
	}

	/**
	 * Interface for a callback to be invoked when the input is done, user 
	 * should implements the search feature(Sync or Async).
	 */
	public interface OnStartSearchListener {
		/**
		 * Callback when input is done, should implements search feature.
		 * 
		 * @param cityName
		 *            The city name that to be searched.
		 */
		void onStartSearch(String cityName);
	}

}
