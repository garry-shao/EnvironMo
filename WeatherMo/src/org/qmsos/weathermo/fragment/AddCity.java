package org.qmsos.weathermo.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.datamodel.City;
import org.qmsos.weathermo.widget.CityRecyclerViewAdapter;

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

public class AddCity extends Fragment {

	private static final String KEY_CANDIDATE_CITIES = "KEY_CANDIDATE_CITIES";

	private CityRecyclerViewAdapter mCityCandidateAdapter;
	private OnInputDoneListener mListener;

	private City[] mCandidateCities = null;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnInputDoneListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnInputDoneListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_add_city, container, false);
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (savedInstanceState != null) {
			mCandidateCities = (City[]) savedInstanceState.getParcelableArray(KEY_CANDIDATE_CITIES);
		}
		
		mCityCandidateAdapter = new CityRecyclerViewAdapter(getContext(), mCandidateCities);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.city_candidates);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(mCityCandidateAdapter);
		
		EditText cityNameEditText = (EditText) view.findViewById(R.id.city_name);
		cityNameEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String cityName = v.getText().toString();
					mListener.onInputDone(cityName);
					
					InputMethodManager manager = (InputMethodManager) getContext().getSystemService(
							Context.INPUT_METHOD_SERVICE);
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
		outState.putParcelableArray(KEY_CANDIDATE_CITIES, mCandidateCities);
		
		super.onSaveInstanceState(outState);
	}

	public void swapData(String result) {
		mCandidateCities = parseResult(result);
		
		mCityCandidateAdapter.swapData(mCandidateCities);
	}

	private City[] parseResult(String result) {
		if (result == null) {
			return null;
		}

		JSONArray list = null;
		int length = 0;
		try {
			JSONObject reader = new JSONObject(result);
			list = reader.getJSONArray("list");
			length = list.length();
		} catch (JSONException e) {
			return null;
		}
		
		if (length == 0) {
			return null;
		}

		City[] candidates = new City[length];
		for (int i = 0; i < length; i++) {
			try {
				JSONObject instance = list.getJSONObject(i);

				long cityId = instance.getLong("id");
				String name = instance.getString("name");

				JSONObject coord = instance.getJSONObject("coord");
				double longitude = coord.getDouble("lon");
				double latitude = coord.getDouble("lat");

				JSONObject sys = instance.getJSONObject("sys");
				String country = sys.getString("country");

				longitude = longitude * 100;
				longitude = Math.round(longitude);
				longitude = longitude / 100;

				latitude = latitude * 100;
				latitude = Math.round(latitude);
				latitude = latitude / 100;

				candidates[i] = new City(cityId, name, country, longitude, latitude);
			} catch (JSONException e) {
				return null;
			}
		}
		
		return candidates;
	}

	/**
	 * Interface for a callback to be invoked when the input is done. 
	 * 
	 *
	 */
	public interface OnInputDoneListener {
		/**
		 * Callback when input is done.
		 * 
		 * @param text
		 *            The input string.
		 */
		void onInputDone(String text);
	}

}
