package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
import org.qmsos.weathermo.WeatherService;
import org.qmsos.weathermo.util.IpcConstants;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is a customized Adapter class used on RecyclerView, basically a mock up
 * of SimpleCursorAdapter class .
 * 
 *
 */
public class RecyclerViewCursorAdapter extends RecyclerViewBaseAdapter<ViewHolder> {
	
	private Context mContext;

	public RecyclerViewCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		
		this.mContext = context;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		final long id = cursor.getLong(cursor.getColumnIndex(WeatherProvider.KEY_CITY_ID));
		String name = cursor.getString(cursor.getColumnIndex(WeatherProvider.KEY_NAME));

		((RecyclerViewHolder) holder).mCityIdView.setText(String.valueOf(id));
		((RecyclerViewHolder) holder).mCityNameView.setText(name);
		((RecyclerViewHolder) holder).mDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(mContext, WeatherService.class);
				i.setAction(IpcConstants.ACTION_DELETE_CITY);
				i.putExtra(IpcConstants.EXTRA_CITY_ID, id);
				mContext.startService(i);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder, null);

		return new RecyclerViewHolder(view);
	}

	static class RecyclerViewHolder extends ViewHolder {
		TextView mCityIdView;
		TextView mCityNameView;
		Button mDeleteButton;

		public RecyclerViewHolder(View itemView) {
			super(itemView);

			mCityIdView = (TextView) itemView.findViewById(R.id.cityId);
			mCityNameView = (TextView) itemView.findViewById(R.id.cityName);
			mDeleteButton = (Button) itemView.findViewById(R.id.cityDelete);
		}
	}

}
