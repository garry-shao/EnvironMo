package org.qmsos.environmo.util;

import org.qmsos.environmo.CityProvider;
import org.qmsos.environmo.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This is a customized Adapter class used on RecyclerView, basically a mock up
 * of SimpleCursorAdapter class .
 * 
 *
 */
public class UtilCursorAdapter extends UtilBaseAdapter<ViewHolder> {

	public UtilCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(CityProvider.KEY_CITYID));
		String name = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_NAME));

		((UtilViewHolder) holder).cityIdView.setText(String.valueOf(id));
		((UtilViewHolder) holder).cityNameView.setText(name);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder, null);

		return new UtilViewHolder(view);
	}

	public static class UtilViewHolder extends ViewHolder {
		TextView cityIdView;
		TextView cityNameView;

		public UtilViewHolder(View itemView) {
			super(itemView);

			cityIdView = (TextView) itemView.findViewById(R.id.cityId);
			cityNameView = (TextView) itemView.findViewById(R.id.cityName);
		}
	}

}
