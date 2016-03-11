package org.qmsos.weathermo.map;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Context;

public class CustomTilesOverlay extends TilesOverlay {

	private final MapTileProviderBase mTileProvider;
	
	public CustomTilesOverlay(MapTileProviderBase tileProvider, Context context) {
		super(tileProvider, context);
		
		mTileProvider = tileProvider;
	}

	public MapTileProviderBase getTileProvider() {
		return mTileProvider;
	}

}
