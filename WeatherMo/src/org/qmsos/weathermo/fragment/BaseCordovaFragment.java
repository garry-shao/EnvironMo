package org.qmsos.weathermo.fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Base customized Fragment class that implements Apache's cordova framework. 
 *
 */
public abstract class BaseCordovaFragment extends Fragment implements CordovaInterface {

	private ExecutorService mThreadPool;
	private CordovaPlugin mPermissionResultCallback;
	private CordovaPlugin mActivityResultCallback;
	private int mActivityResultRequestCode;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mThreadPool = Executors.newCachedThreadPool();
	}

	@Override
	public ExecutorService getThreadPool() {
		return mThreadPool;
	}

	@TargetApi(23)
	@Override
	public boolean hasPermission(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int result = getContext().checkSelfPermission(permission);
			
			return PackageManager.PERMISSION_GRANTED == result;
		} else {
			return true;
		}
	}

	@Override
	public Object onMessage(String id, Object data) {
		return null;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (mPermissionResultCallback != null) {
			try {
				mPermissionResultCallback.onRequestPermissionResult(
						requestCode, permissions, grantResults);
			} catch (JSONException e) {
				mPermissionResultCallback = null;
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
		mPermissionResultCallback = plugin;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			String[] permissions = { permission };
			requestPermissions(permissions, requestCode);
		}
	}

	@Override
	public void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {
		mPermissionResultCallback = plugin;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(permissions, requestCode);
		}
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
		// Cancel any previously pending activity.
		if (mActivityResultCallback != null) {
			mActivityResultCallback.onActivityResult(
					mActivityResultRequestCode, Activity.RESULT_CANCELED, null);
		} else {
			mActivityResultCallback = plugin;
		}
	}

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
		setActivityResultCallback(command);

		try {
			startActivityForResult(intent, requestCode);
		} catch (RuntimeException e) {
			mActivityResultCallback = null;
			throw e;
		}
	}

}
