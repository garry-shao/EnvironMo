package org.qmsos.weathermo.fragment;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseLongArray;
import android.view.ViewGroup;

import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;

/**
 * Customized PagerAdapter that contains 
 * {@linkplain WeatherDetails WeatherDetails}.
 */
public class WeatherDetailsPagerAdapter extends FragmentStatePagerAdapter {

    private boolean mDataValid;
    private Cursor mCursor;
    private Context mContext;

    private SparseLongArray mCityIds;

    public WeatherDetailsPagerAdapter(FragmentManager fm, Context context, Cursor cursor) {
        super(fm);

        init(context, cursor);
    }

    private void init(Context context, Cursor cursor) {
        mDataValid = cursor != null;
        mCursor = cursor;
        mContext = context;

        mCityIds = new SparseLongArray();
    }

    @Override
    public Fragment getItem(int position) {
        if (mDataValid && mCursor != null) {
            return getItem(mContext, mCursor, position);
        } else {
            return null;
        }
    }

    private Fragment getItem(Context context, Cursor cursor, int position) {
        if (cursor.moveToPosition(position)) {
            long cityId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(WeatherEntity.CITY_ID));

            return WeatherDetails.newInstance(context, cityId);
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
        mCityIds.delete(position);

        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCursor.moveToPosition(position)) {
            long cityId = mCursor.getLong(
                    mCursor.getColumnIndexOrThrow(WeatherEntity.CITY_ID));
            mCityIds.append(position, cityId);
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
        mDataValid = newCursor != null;

        notifyDataSetChanged();

        return oldCursor;
    }

    public long getCityId(int position) {
        if (mCityIds != null) {
            return mCityIds.get(position);
        } else {
            return 0;
        }
    }
}