package org.qmsos.weathermo.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * This is basically a mock up of CursorAdapter class with minimum feature, implements 
 * to show another view type on tail of cursor views. 
 *
 * @param <VH>
 *            subclass of ViewHolder.
 */
public abstract class BaseCursorRecyclerViewAdapter<VH extends ViewHolder> extends Adapter<VH> {

	public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;
	
	protected static final int VIEW_TYPE_CURSOR = 0x00;
	protected static final int VIEW_TYPE_OTHER = 0x02;

	protected Cursor mCursor;
	protected Context mContext;
	protected int mRowIDColumn;
	protected boolean mDataValid;
	protected ChangeObserver mChangeObserver;
	protected DataSetObserver mDataSetObserver;

	public BaseCursorRecyclerViewAdapter(Context context, Cursor c, int flags) {
		init(context, c, flags);
	}

	public BaseCursorRecyclerViewAdapter(Context context, Cursor c) {
		this(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
	}

	private void init(Context context, Cursor c, int flags) {
		boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mContext = context;
		mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
		if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
			mChangeObserver = new ChangeObserver();
			mDataSetObserver = new MyDataSetObserver();
		} else {
			mChangeObserver = null;
			mDataSetObserver = null;
		}

		if (cursorPresent) {
			if (mChangeObserver != null)
				c.registerContentObserver(mChangeObserver);
			if (mDataSetObserver != null)
				c.registerDataSetObserver(mDataSetObserver);
		}

		setHasStableIds(true);
	}

	@Override
	public int getItemCount() {
		if (mDataValid && mCursor != null) {
			return mCursor.getCount() + 1;
		} else {
			return 0;
		}
	}

	@Override
	public long getItemId(int position) {
		if (mDataValid && mCursor != null && (position < mCursor.getCount())) {
			if (mCursor.moveToPosition(position)) {
				return mCursor.getLong(mRowIDColumn);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (mDataValid && mCursor != null && (position < mCursor.getCount())) {
			return VIEW_TYPE_CURSOR;
		} else {
			return VIEW_TYPE_OTHER;
		}
	}

	@Override
	public void onBindViewHolder(VH holder, int position) {
		if (!mDataValid) {
			throw new IllegalStateException("cursor data is invalid!");
		}

		if (position < mCursor.getCount()) {
			if (mCursor.moveToPosition(position)) {
				onBindViewHolderCursor(holder, mCursor);
			} else {
				throw new IllegalStateException("moving cursor to position " + position + " failed.");
			}
		} else {
			onBindViewHolderOther(holder);
		}
	}

	public abstract void onBindViewHolderCursor(VH holder, Cursor cursor);

	public abstract void onBindViewHolderOther(VH holder);

	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		Cursor oldCursor = mCursor;
		if (oldCursor != null) {
			if (mChangeObserver != null)
				oldCursor.unregisterContentObserver(mChangeObserver);
			if (mDataSetObserver != null)
				oldCursor.unregisterDataSetObserver(mDataSetObserver);
		}
		mCursor = newCursor;
		if (newCursor != null) {
			if (mChangeObserver != null)
				newCursor.registerContentObserver(mChangeObserver);
			if (mDataSetObserver != null)
				newCursor.registerDataSetObserver(mDataSetObserver);
			mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
			mDataValid = true;

			notifyDataSetChanged();
		} else {
			mRowIDColumn = -1;
			mDataValid = false;

			notifyDataSetChanged();
		}
		return oldCursor;
	}

	protected void onContentChanged() {
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			mDataValid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mDataValid = false;
			notifyDataSetChanged();
		}
	}

}
