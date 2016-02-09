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
public class RecyclerViewCursorAdapter extends RecyclerViewBaseAdapter<ViewHolder> {
	
	private Context mContext;

	public RecyclerViewCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		
		this.mContext = context;
	}

	@Override
	public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {
		final long id = cursor.getLong(cursor.getColumnIndex(WeatherProvider.KEY_CITY_ID));
		String name = cursor.getString(cursor.getColumnIndex(WeatherProvider.KEY_NAME));
		
		((CursorViewHolder) holder).mCityIdView.setText(String.valueOf(id));
		((CursorViewHolder) holder).mCityNameView.setText(name);
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
		TextView mCityIdView;
		TextView mCityNameView;
		Button mDeleteButton;

		public CursorViewHolder(View itemView) {
			super(itemView);

			mCityIdView = (TextView) itemView.findViewById(R.id.cityId);
			mCityNameView = (TextView) itemView.findViewById(R.id.cityName);
			mDeleteButton = (Button) itemView.findViewById(R.id.cityDelete);
		}
	}
	
	static class AddViewHolder extends ViewHolder {
		Button mAddButton;
		
		public AddViewHolder(View itemView) {
			super(itemView);
			
			mAddButton = (Button) itemView.findViewById(R.id.cityAdd);
		}
	}

}
