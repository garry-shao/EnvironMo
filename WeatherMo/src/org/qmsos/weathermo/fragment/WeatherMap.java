package org.qmsos.weathermo.fragment;

import java.util.List;

import org.osmdroid.views.overlay.Overlay;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.ProviderContract.CityEntity;
import org.qmsos.weathermo.map.CustomTilesOverlay;
import org.qmsos.weathermo.map.TileFilesChecker;
import org.qmsos.weathermo.widget.CustomMapView;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Show weather map.
 *
 */
public class WeatherMap extends Fragment implements LoaderCallbacks<Cursor> {

	private static final String KEY_CITY_ID = "KEY_CITY_ID";
	
	private CustomMapView mMapView;

	/**
	 * Create a new instance that shows weather map.
	 * 
	 * @param cityId
	 *            The city id of the map that currently showing.
	 * @return The created fragment instance.
	 */
	public static WeatherMap newInstance(long cityId) {
		Bundle args = new Bundle();
		args.putLong(KEY_CITY_ID, cityId);
		
		WeatherMap fragment = new WeatherMap();
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		TileFilesChecker.checkMapTileFiles(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_weather_map, container, false);
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mMapView = (CustomMapView) view.findViewById(R.id.weather_map);
		mMapView.setMultiTouchControls(true);
		mMapView.setTilesScaledToDpi(true);
		mMapView.setUseDataConnection(false);
		mMapView.setTileSource(TileFilesChecker.offlineTileSource());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		
		super.onDestroyView();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long cityId = getArguments().getLong(KEY_CITY_ID, -1L);
		
		String[] projection = { CityEntity.LONGITUDE, CityEntity.LATITUDE };
		String where = CityEntity.CITY_ID + " = " + cityId;
		
		return new CursorLoader(getContext(), CityEntity.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null && data.moveToFirst()) {
			double latitude = data.getDouble(data.getColumnIndexOrThrow(CityEntity.LATITUDE));
			double longitude = data.getDouble(data.getColumnIndexOrThrow(CityEntity.LONGITUDE));
			
			mMapView.setCenter(latitude, longitude);
			mMapView.getController().setZoom(4);
		}
		
		loadMap(null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Show weather map with to specified layer.
	 * 
	 * @param layer
	 *            The name of the layer that will be showed, NULL means default layer.
	 */
	public void loadMap(String layer) {
		String name = (layer == null) ? "temp" : layer;
		
		List<Overlay> overlays = mMapView.getOverlays();
		int overlaySize = overlays.size();
		if (overlaySize > 0) {
			CustomTilesOverlay layerOverlay = (CustomTilesOverlay) overlays.get(overlaySize - 1);
			layerOverlay.getTileProvider().clearTileCache();
			
			overlays.remove(overlaySize - 1);
		}
		overlays.add(TileFilesChecker.onlineTilesOverlay(getContext(), name));
	}

}
