package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.AddActivity;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
import org.qmsos.weathermo.WeatherService;
import org.qmsos.weathermo.util.IpcConstants;

import android.content.Context;
import android.content.Intent;
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
 * This is a customized Adapter class used on RecyclerView, can show another view 
 * on tail of the regular cursor views.
 *
 */
public class CursorRecyclerViewAdapter extends BaseCursorRecyclerViewAdapter<ViewHolder> {
	
	private Context mContext;

	public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		
		this.mContext = context;
	}

	@Override
	public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {
		final long id = cursor.getLong(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CITY_ID));
		String name = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_NAME));
		String country = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_COUNTRY));
		double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_LONGITUDE));
		double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_LATITUDE));
		
		String nameRaw = name + " " +country;
		
		SpannableString spanned = new SpannableString(nameRaw);
		spanned.setSpan(new RelativeSizeSpan(0.5f), 
				name.length() + 1, nameRaw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		String lon = longitude > 0 ? Math.abs(longitude) + "\u00b0E" : Math.abs(longitude) + "\u00b0W";
		String lat = latitude > 0 ? Math.abs(latitude) + "\u00b0N" : Math.abs(latitude) + "\u00b0S";
		
		((CursorViewHolder) holder).mCityInfoView.setText(spanned);
		((CursorViewHolder) holder).mCityGeoView.setText(lon + " " + lat);
		((CursorViewHolder) holder).mDeleteButton.setOnClickListener(new OnClickListener() {
			
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
	public void onBindViewHolderOther(ViewHolder holder) {
		((AddViewHolder) holder).mAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(mContext, AddActivity.class);
				mContext.startActivity(i);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
		case VIEW_TYPE_CURSOR:
			View view = View.inflate(parent.getContext(), R.layout.view_holder_cursor, null);
			
			return new CursorViewHolder(view);
		case VIEW_TYPE_OTHER:
			View addView = View.inflate(parent.getContext(), R.layout.view_holder_add, null);
			
			return new AddViewHolder(addView);
		default:
			return null;
		}
	}

	static class CursorViewHolder extends ViewHolder {
		TextView mCityInfoView;
		TextView mCityGeoView;
		Button mDeleteButton;

		public CursorViewHolder(View itemView) {
			super(itemView);

			mCityInfoView = (TextView) itemView.findViewById(R.id.city_info);
			mCityGeoView = (TextView) itemView.findViewById(R.id.city_geo);
			mDeleteButton = (Button) itemView.findViewById(R.id.city_delete);
		}
	
	}
	
	static class AddViewHolder extends ViewHolder {
		Button mAddButton;
		
		public AddViewHolder(View itemView) {
			super(itemView);
			
			mAddButton = (Button) itemView.findViewById(R.id.city_add);
		}
	
	}

}
