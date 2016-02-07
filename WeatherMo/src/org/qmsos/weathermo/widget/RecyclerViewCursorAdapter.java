package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
import org.qmsos.weathermo.WeatherService;

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
	
	private Context context;

	public RecyclerViewCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		
		this.context = context;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		final long id = cursor.getLong(cursor.getColumnIndex(WeatherProvider.KEY_CITY_ID));
		String name = cursor.getString(cursor.getColumnIndex(WeatherProvider.KEY_NAME));

		((UtilViewHolder) holder).cityIdView.setText(String.valueOf(id));
		((UtilViewHolder) holder).cityNameView.setText(name);
		((UtilViewHolder) holder).deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, WeatherService.class);
				i.setAction(WeatherService.ACTION_DELETE_CITY);
				i.putExtra(WeatherService.EXTRA_KEY_CITY_ID, id);
				context.startService(i);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder, null);

		return new UtilViewHolder(view);
	}

	public static class UtilViewHolder extends ViewHolder {
		TextView cityIdView;
		TextView cityNameView;
		Button deleteButton;

		public UtilViewHolder(View itemView) {
			super(itemView);

			cityIdView = (TextView) itemView.findViewById(R.id.cityId);
			cityNameView = (TextView) itemView.findViewById(R.id.cityName);
			deleteButton = (Button) itemView.findViewById(R.id.cityDelete);
		}
	}

}
