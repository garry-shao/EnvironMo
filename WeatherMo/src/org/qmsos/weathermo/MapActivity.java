package org.qmsos.weathermo;

import org.qmsos.weathermo.contract.IntentContract;
import org.qmsos.weathermo.fragment.WeatherMap;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.KeyEvent;
import android.view.MenuItem;

/**
 * Activity that showing map of current weather. 
 *
 */
public class MapActivity extends AppCompatActivity implements OnMenuItemClickListener {

	private static final String FRAGMENT_TAG_WEATHER_MAP = "FRAGMENT_TAG_WEATHER_MAP";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.activity_weather_map);
		toolbar.inflateMenu(R.menu.menu_map_layer);
		toolbar.setOnMenuItemClickListener(this);
		
		long cityId = getIntent().getLongExtra(IntentContract.EXTRA_CITY_ID, -1);
		WeatherMap weatherMap = WeatherMap.newInstance(cityId);
		
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.fragment_container, weatherMap, FRAGMENT_TAG_WEATHER_MAP);
		transaction.commit();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			if (toolbar != null) {
				if (toolbar.isOverflowMenuShowing()) {
					toolbar.hideOverflowMenu();
				} else {
					toolbar.showOverflowMenu();
				}
				
				return true;
			} else {
				return super.onKeyUp(keyCode, event);
			}
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		String layer;
		switch (item.getItemId()) {
		case R.id.menu_layer_precipitation:
			layer = LayerContract.LAYER_PRECIPITATION;
			break;
		case R.id.menu_layer_rain:
			layer = LayerContract.LAYER_RAIN;
			break;
		case R.id.menu_layer_snow:
			layer = LayerContract.LAYER_SNOW;
			break;
		case R.id.menu_layer_clouds:
			layer = LayerContract.LAYER_CLOUDS;
			break;
		case R.id.menu_layer_pressure:
			layer = LayerContract.LAYER_PRESSURE;
			break;
		case R.id.menu_layer_temperature:
			layer = LayerContract.LAYER_TEMPERATURE;
			break;
		case R.id.menu_layer_windspeed:
			layer = LayerContract.LAYER_WINDSPEED;
			break;
		default:
			layer = null;
		}
		loadMap(layer);
		
		return true;
	}

	/**
	 * Proxy method that load specified layer of weather map.
	 * 
	 * @param layer
	 *            The name of the map layer, should be those values who are contracted in 
	 *            class {@linkplain org.qmsos.weathermo.MapActivity.LayerContract LayerContract}.
	 */
	private void loadMap(String layer) {
		WeatherMap weatherMap = (WeatherMap) 
				getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_WEATHER_MAP);
		if (weatherMap != null && weatherMap.isAdded()) {
			weatherMap.loadMap(layer);
		}
	}

	/**
	 * Contract class that contains layer name of weather map.
	 */
	private static final class LayerContract {
		
		static final String LAYER_PRECIPITATION = "precipitation";
		static final String LAYER_RAIN = "rain";
		static final String LAYER_SNOW = "snow";
		static final String LAYER_CLOUDS = "clouds";
		static final String LAYER_PRESSURE = "pressure";
		static final String LAYER_TEMPERATURE = "temp";
		static final String LAYER_WINDSPEED = "wind";
	
	}

}
