package org.qmsos.weathermo;

import org.qmsos.weathermo.contract.IntentContract;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Execute update service whenever specified intent received.
 *
 *
 */
public class WeatherAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action != null && action.equals(IntentContract.ACTION_REFRESH_ALARM)) {
			Intent refreshIntent = new Intent(context, WeatherService.class);
			refreshIntent.setAction(IntentContract.ACTION_REFRESH_WEATHER_AUTO);
			refreshIntent.putExtra(IntentContract.EXTRA_REFRESH_WEATHER_AUTO, true);
			
			context.startService(refreshIntent);
		}
	}

}
