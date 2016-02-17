package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.datamodel.City;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Adapter that shows data of cities.
 * 
 *
 */
public class CityRecyclerViewAdapter extends BaseArrayRecyclerViewAdapter<City, ViewHolder> {

	private OnInsertCityClickedListener mListener;

	public CityRecyclerViewAdapter(Context context, City[] dataArray) {
		super(context, dataArray);
		
		try {
			mListener = (OnInsertCityClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnInsertCityClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, City data) {
		final City dataCopy = data;
		
		String info = data.getCityName() + " " + data.getCountry();
		
		((RecyclerViewHolder) holder).mCityInfoView.setText(info);		
		((RecyclerViewHolder) holder).mInsertButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onInsertCity(dataCopy);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.view_holder_insert, null);
		
		return new RecyclerViewHolder(view);
	}

	static class RecyclerViewHolder extends ViewHolder {
		TextView mCityInfoView;
		Button mInsertButton;
		
		public RecyclerViewHolder(View itemView) {
			super(itemView);
			
			mCityInfoView = (TextView) itemView.findViewById(R.id.city_info);
			mInsertButton = (Button) itemView.findViewById(R.id.city_insert);
		}
	
	}

	/**
	 * Interface for a callback to be invoked when the adding city button of ViewHolder 
	 * class is called.
	 * 
	 *
	 */
	public interface OnInsertCityClickedListener {
		/**
		 * Callback to insert city.
		 * 
		 * @param city
		 *            The city to insert.
		 */
		void onInsertCity(City city);
	}

}
