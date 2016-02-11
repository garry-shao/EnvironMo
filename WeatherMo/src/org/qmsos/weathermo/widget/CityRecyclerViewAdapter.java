package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherService;
import org.qmsos.weathermo.util.City;
import org.qmsos.weathermo.util.IpcConstants;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Adapter that shows data of cities.
 * 
 *
 */
public class CityRecyclerViewAdapter extends BaseArrayRecyclerViewAdapter<City, ViewHolder> {

	private Context mContext;
	
	public CityRecyclerViewAdapter(Context context) {
		mContext = context;
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, City data) {
		final City dataCopy = data;
		
		((RecyclerViewHolder) holder).mCandidateAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(mContext, WeatherService.class);
				i.setAction(IpcConstants.ACTION_ADD_CITY);
				i.putExtra(IpcConstants.EXTRA_ADD_CITY, dataCopy);
				
				mContext.startService(i);
			}
		});
		
		String info = data.getCityName() + " " + data.getCountry();
		((RecyclerViewHolder) holder).mCandidateInfoView.setText(info);		
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder_candidate, null);
		
		return new RecyclerViewHolder(view);
	}

	static class RecyclerViewHolder extends ViewHolder {
		TextView mCandidateInfoView;
		Button mCandidateAddButton;
		
		public RecyclerViewHolder(View itemView) {
			super(itemView);
			
			mCandidateInfoView = (TextView) itemView.findViewById(R.id.candidate_info);
			mCandidateAddButton = (Button) itemView.findViewById(R.id.candidate_add);
		}
	
	}

}
