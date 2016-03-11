package org.qmsos.weathermo.map;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import android.content.Context;
import android.graphics.Color;

/**
 * Utility class use to checker if the offline map tiles used in MapView class
 * are available on storage device.
 * 
 *
 */
public class TileFilesChecker {

	private static final String OFFLINE_MAP_SOURCE = "Mapnik";
	private static final String OFFLIEN_MAP_TILE_FILE = OFFLINE_MAP_SOURCE + ".zip";
	private static final String OFFLINE_MAP_TILE_HASH = OFFLINE_MAP_SOURCE + ".sha";
	private static final String ONLINE_MAP_SOURCE = "OpenWeatherMap";

	// Zoom levels of the offline map-tile source.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;
	
	// Max retry count before abort.
	private static final int MAX_RETRY = 3;

	/**
	 * Create offline map-tile source.
	 * 
	 * @return The offline map-tile source.
	 */
	public static ITileSource offlineTileSource() {
		return new XYTileSource(OFFLINE_MAP_SOURCE, 
				ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {});
	}
	
	/**
	 * 
	 * @param context
	 * @param layerName
	 * @return
	 */
	public static CustomTilesOverlay onlineTilesOverlay(Context context, String layerName) {
		XYTileSource onlineResource = new XYTileSource(ONLINE_MAP_SOURCE, 
				ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {
						"http://a.tile.openweathermap.org/map/" + layerName + "/",
						"http://b.tile.openweathermap.org/map/" + layerName + "/",
						"http://c.tile.openweathermap.org/map/" + layerName + "/" });
		
		CustomMapTileProvider onlineProvider = new CustomMapTileProvider(context, onlineResource);
		
		CustomTilesOverlay onlineLayer = new CustomTilesOverlay(onlineProvider, context);
		onlineLayer.setLoadingBackgroundColor(Color.TRANSPARENT);
		onlineLayer.setLoadingBackgroundColor(Color.TRANSPARENT);
		onlineLayer.setUseDataConnection(true);
		
		return onlineLayer;
	}

	/**
	 * Check whether offline map tiles are available, create new ones if not.
	 * 
	 * @param context
	 *            The associated context.
	 */
	public static void checkMapTileFiles(Context context) {
		// Change osmdroid's path, but there are still bugs: since the paths are 
		// static final fields, the tiles-base path created before the change, so
		// that still created on old configuration, but since we are using offline
		// map-tiles, blocking this file creation by revoking the permission does
		// no harm except an error line on log.
		String cachePath = context.getCacheDir().getAbsolutePath();
		String filePath = context.getFilesDir().getAbsolutePath();
		OpenStreetMapTileProviderConstants.setCachePath(cachePath);
		OpenStreetMapTileProviderConstants.setOfflineMapsPath(filePath);
		
		boolean flag = false;
		for (int i = 0; i < MAX_RETRY && !flag; i++) {
			File mapTileFile = 
					new File(OpenStreetMapTileProviderConstants.getBasePath(), OFFLIEN_MAP_TILE_FILE);
			
			if (mapTileFile.exists() || copyFiles(context, OFFLIEN_MAP_TILE_FILE, mapTileFile)) {
				flag = hashFiles(context, OFFLINE_MAP_TILE_HASH, mapTileFile);
			}
		}
	}

	/**
	 * Copy file from assets in APK to specific path.
	 * 
	 * @param context
	 *            The associated context.
	 * @param assetsFilename
	 *            The name of the file to be copied in assets of APK. 
	 * @param targetFilePath
	 *            Targeted file path.
	 * @return TRUE if copying succeeded, FALSE otherwise.
	 */
	private static boolean copyFiles(Context context, String assetsFilename, File targetFilePath) {
		boolean flag = false;

		InputStream in = null;
		BufferedOutputStream bout = null;
		try {
			in = context.getAssets().open(assetsFilename);
			
			byte[] buffer = new byte[2048];
			
			bout = new BufferedOutputStream(new FileOutputStream(targetFilePath), buffer.length);
			
			int content;
			while ((content = in.read(buffer)) != -1) {
				bout.write(buffer, 0, content);
			}
			flag = true;
		} catch (IOException e) {
			flag = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					flag = false;
				}
			}
			if (bout != null) {
				try {
					bout.flush();
					bout.close();
				} catch (IOException e) {
					flag = false;
				}
			}
		}
		
		return flag;
	}

	/**
	 * Hash file to decide whether it is authentic.
	 * 
	 * @param context
	 *            The associated context.
	 * @param assetsHashFilename
	 *            The name of file containing hash info in assets of APK.
	 * @param targetFilePath
	 *            Targeted file path to be hashed.
	 * @return TRUE if the comparison succeeded, FALSE otherwise.
	 */
	private static boolean hashFiles(Context context, String assetsHashFilename, File targetFilePath) {
		boolean flagChecksum = false;
		String fileChecksum = null;
		InputStream in = null;
		try {
			in = context.getAssets().open(assetsHashFilename);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			fileChecksum = reader.readLine();
			flagChecksum = true;
		} catch (IOException e) {
			flagChecksum = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					flagChecksum = false;
				}
			}
		}

		boolean flagHash = false;
		String fileHash = null;
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			in = new FileInputStream(targetFilePath);
			
			byte[] buffer = new byte[2048];
			
			int content;
			while ((content = in.read(buffer)) != -1) {
				digest.update(buffer, 0, content);
			}
			
			byte[] sha1 = digest.digest();
			
			BigInteger bigInt = new BigInteger(1, sha1);
			fileHash = bigInt.toString(16);
			flagHash = true;
		} catch (IOException e) {
			flagHash = false;
		} catch (NoSuchAlgorithmException e) {
			flagHash = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					flagHash = false;
				}
			}
		}
		
		if (flagChecksum && flagHash && 
				fileChecksum != null && fileHash != null && fileChecksum.equals(fileHash)) {
			
			return true;
		} else {
			return false;
		}
	}

}
