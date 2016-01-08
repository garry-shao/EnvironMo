package org.qmsos.environmo.util;

import org.qmsos.environmo.CityProvider;
import org.qmsos.environmo.fragment.CurrentFragment;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseLongArray;
import android.view.ViewGroup;

public class UtilPagerAdapter extends FragmentStatePagerAdapter {

	private boolean mDataValid;
	private Cursor mCursor;
	private Context mContext;
	
	private SparseLongArray ids;
	
	public UtilPagerAdapter(FragmentManager fm, Context context, Cursor cursor) {
		super(fm);

		init(context, cursor);
	}
	
	private void init(Context context, Cursor cursor) {
		boolean cursorPresent = cursor != null;
		mDataValid = cursorPresent;
		mCursor = cursor;
		mContext = context;
		
		ids = new SparseLongArray();
	}

	@Override
	public Fragment getItem(int position) {
		if (mDataValid && mCursor != null) {
			return getItem(mContext, mCursor, position);
		} else {
			return null;
		}
	}

	public Fragment getItem(Context context, Cursor cursor, int position) {
		if (cursor.moveToPosition(position)) {
			long cityId = cursor.getLong(cursor.getColumnIndexOrThrow(CityProvider.KEY_CITYID));
			CurrentFragment fragment = CurrentFragment.newInstance(context, cityId);
			
			return fragment;
		} else {
			return null;
		}
	}

	@Override
	public int getItemPosition(Object object) {
//		TODO: do not destroy all fragments later.
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		if (mDataValid && mCursor != null) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		ids.delete(position);

		super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (mCursor.moveToPosition(position)) {
			long cityId = mCursor.getLong(mCursor.getColumnIndexOrThrow(CityProvider.KEY_CITYID));
			ids.append(position, cityId);
		}
		
		return super.instantiateItem(container, position);
	}

	public void changeCursor(Cursor newCursor) {
		Cursor oldCursor = swapCursor(newCursor);
		if (oldCursor != null) {
			oldCursor.close();
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		
		Cursor oldCursor = mCursor;
		mCursor = newCursor;
		if (newCursor != null) {
			mDataValid = true;
		} else {
			mDataValid = false;
		}
		notifyDataSetChanged();
		
		return oldCursor;
	}

	public long getId(int position) {
		if (ids != null) {
			return ids.get(position);
		} else {
			return 0;
		}
	}

}
