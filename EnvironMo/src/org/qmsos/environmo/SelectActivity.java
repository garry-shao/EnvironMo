package org.qmsos.environmo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SelectActivity extends Activity {

	public static final String CITY_NAME = "org.qmsos.environmo.CITY_NAME";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		finish();
	}

	public void settingCityId(View view) {
		EditText editText = (EditText) findViewById(R.id.setting_city_text);
		String cityname = editText.getText().toString();
		
		PendingIntent pendingResult = createPendingResult(0, new Intent(), 0);
		
		Intent intent = new Intent(getApplicationContext(), MainUpdateService.class);
		intent.putExtra(MainUpdateService.EXTRA_PENDING_RESULT, pendingResult);
		intent.putExtra(CITY_NAME, cityname);
		intent.putExtra(MainUpdateService.QUERY_CITY, true);
		
		startService(intent);
	}
	
}
