package org.qmsos.weathermo.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Customized Adapter of RecyclerView inflating array of data.
 *
 * @param <T>
 *            Class type of the array of data. 
 * @param <VH>
 *            Subclass of ViewHolder.
 */
public abstract class BaseArrayRecyclerViewAdapter<T, VH extends ViewHolder> extends Adapter<VH> {
	
	private T[] mDataArray;

	public BaseArrayRecyclerViewAdapter(Context context, T[] dataArray) {
		this.mDataArray = dataArray;
	}

	@Override
	public int getItemCount() {
		return mDataArray == null ? 0 : mDataArray.length;
	}

	@Override
	public void onBindViewHolder(VH holder, int position) {
		if (mDataArray == null || position >= mDataArray.length) {
			return;
		}
		
		T data = mDataArray[position];
		onBindViewHolder(holder, data);
	}

	public abstract void onBindViewHolder(VH holder, T data);

	public void swapData(T[] dataArray) {
		mDataArray = dataArray;
		
		notifyDataSetChanged();
	}

}
