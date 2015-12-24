package org.qmsos.environmo.util;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class UtilPagerAdapter extends FragmentPagerAdapter {

	private SparseArray<String> tags = new SparseArray<String>();
	private List<Fragment> fragmentList;

	public UtilPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
		super(fm);
		
		this.fragmentList = fragmentList;
	}

	@Override
	public Fragment getItem(int arg0) {
		return (fragmentList == null || fragmentList.size() == 0) ? null : fragmentList.get(arg0);
	}

	@Override
	public int getCount() {
		return fragmentList == null ? 0 : fragmentList.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		String tag = fragment.getTag();
		tags.append(position, tag);

		return fragment;
	}

	public String getTag(int position) {
		return tags.get(position);
	}

}
