package org.qmsos.environmo;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingCityActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_city_activity);
	}
	
	public void settingCityId(View view) {
		EditText editText = (EditText) findViewById(R.id.setting_city_text);
		
		searchCityId(editText.getText().toString());
		
		Toast.makeText(getApplicationContext(), "Succeded!!", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void searchCityId(String cityName) {
		final String testQuery = "http://api.openweathermap.org/data/2.5/" +
				"weather?" + 
				"q=" + cityName + 
				"&units=" + "metric" + 
				"&appid=" + MainUpdateService.API_KEY;
		
		class AsyncQuery extends AsyncTask<Void, Void, Integer> {

			@Override
			protected Integer doInBackground(Void... params) {
				String testJSON;
				try {
					testJSON = MainUpdateService.queryAsJSON(testQuery);
					JSONObject reader = new JSONObject(testJSON);
					final int i = reader.getInt("id");
					return i;
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				} catch (JSONException e) {
					e.printStackTrace();
					return -1;
				}
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				
				storeCityId(result);
			}
		}
			
		new AsyncQuery().execute();
	}
	
	private void storeCityId(int cityId) {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		if (cityId != -1) {
			editor.putInt(MainUpdateService.CITY_ID, cityId);
			editor.apply();
		}
	}
}
