package org.qmsos.weathermo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.weathermo.util.City;
import org.qmsos.weathermo.util.IpcConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {

	private MessageReceiver mMessageReceiver;
	private CandidateAdapter mCandidateAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		mMessageReceiver = new MessageReceiver();
		
		mCandidateAdapter = new CandidateAdapter(this);
		RecyclerView candidatesView = (RecyclerView) findViewById(R.id.city_candidates);
		candidatesView.setLayoutManager(new LinearLayoutManager(this));
		candidatesView.setAdapter(mCandidateAdapter);
		
		InputFilter noWhitespaceFilter = new InputFilter() {

			@Override
			public CharSequence filter(
					CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (Character.isSpaceChar(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		
		EditText cityNameEditText = (EditText) findViewById(R.id.city_name);
		cityNameEditText.setFilters(new InputFilter[] { noWhitespaceFilter });
		cityNameEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String cityName = v.getText().toString();
					
					Intent intent = new Intent(getBaseContext(), WeatherService.class);
					intent.setAction(IpcConstants.ACTION_QUERY_CITY);
					intent.putExtra(IpcConstants.EXTRA_CITY_NAME, cityName);
					
					startService(intent);
					
					InputMethodManager manager = 
							(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					
					return true;
				} else {
					return false;
				}
				
			}
		});
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(IpcConstants.ACTION_QUERY_EXECUTED);
		filter.addAction(IpcConstants.ACTION_ADD_EXECUTED);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
	}

	private void parseSearchResult(String result) {
		if (result == null) {
			return;
		}
		
		try {
			JSONObject reader = new JSONObject(result);
			JSONArray list = reader.getJSONArray("list");
			
			int length = list.length();
			City[] candidates = new City[length];
			for (int i = 0; i < length; i++) { 
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
			}
			
			mCandidateAdapter.swapData(candidates);
		} catch (JSONException e) {
		}
	}

	private class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(IpcConstants.ACTION_QUERY_EXECUTED)) {
					boolean flag = intent.getBooleanExtra(IpcConstants.EXTRA_QUERY_EXECUTED, false);
					if (flag) {
						String result = intent.getStringExtra(IpcConstants.EXTRA_QUERY_RESULT);
						parseSearchResult(result);
					}
				} else if (action.equals(IpcConstants.ACTION_ADD_EXECUTED)) {
					boolean flag = intent.getBooleanExtra(IpcConstants.EXTRA_ADD_EXECUTED, false);
					String text = flag ? "Succeeded" : "Failed";
					Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private class CandidateAdapter extends Adapter<CandidateAdapter.CandidateViewHolder> {

		private City[] mDataArray;
		private Context mContext;
		
		public CandidateAdapter(Context context) {
			mContext = context;
		}
		
		@Override
		public int getItemCount() {
			return mDataArray == null ? 0 : mDataArray.length;
		}

		@Override
		public void onBindViewHolder(CandidateViewHolder holder, int position) {
			if (mDataArray == null || position >= mDataArray.length) {
				return;
			}
			
			final City city = mDataArray[position];
			
			holder.mCandidateAddButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(mContext, WeatherService.class);
					i.setAction(IpcConstants.ACTION_ADD_CITY);
					i.putExtra(IpcConstants.EXTRA_ADD_CITY, city);
					
					mContext.startService(i);
				}
			});
			holder.mCandidateInfoView.setText(city.getCityName() + " " + city.getCountry());
		}

		@Override
		public CandidateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = View.inflate(parent.getContext(), R.layout.view_holder_candidate, null);
			
			return new CandidateViewHolder(view);
		}
		
		public void swapData(City[] dataArray) {
			mDataArray = dataArray;
			
			notifyDataSetChanged();
		}
		
		private class CandidateViewHolder extends ViewHolder {
			TextView mCandidateInfoView;
			Button mCandidateAddButton;
			
			public CandidateViewHolder(View itemView) {
				super(itemView);
				
				mCandidateInfoView = (TextView) itemView.findViewById(R.id.candidate_info);
				mCandidateAddButton = (Button) itemView.findViewById(R.id.candidate_add);
			}
			
		}
	
	}

}
