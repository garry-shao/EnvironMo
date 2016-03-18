package org.qmsos.weathermo;

import org.qmsos.weathermo.fragment.PreferenceMain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Activity that shows settings. 
 *
 */
public class SettingsActivity extends AppCompatActivity {

	private static final String FRAGMENT_TAG_SETTINGS = "FRAGMENT_TAG_SETTINGS";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		FragmentManager manager = getSupportFragmentManager();
		
		PreferenceMain settings = null;
		Fragment fragmentSettings = manager.findFragmentByTag(FRAGMENT_TAG_SETTINGS);
		if (fragmentSettings == null) {
			settings = new PreferenceMain();
			
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.fragment_container, settings, FRAGMENT_TAG_SETTINGS);
			transaction.commit();
		}
	}

}
