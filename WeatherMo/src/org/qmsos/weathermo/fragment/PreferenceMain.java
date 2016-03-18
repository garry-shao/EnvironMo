package org.qmsos.weathermo.fragment;


import org.qmsos.weathermo.R;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class PreferenceMain extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preference);		
	}

}
