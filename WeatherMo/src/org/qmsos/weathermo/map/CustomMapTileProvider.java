package org.qmsos.weathermo.map;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class CustomMapTileProvider extends MapTileProviderBasic {

	private static final int TILE_SIZE_PIXEL = 256;
	
	private Context mContext;
	private Paint mPaint;
	
	public CustomMapTileProvider(Context context, ITileSource tileSource) {
		super(context, tileSource);
		
		mContext = context;
		mPaint = new Paint();
		mPaint.setAlpha(128);
	}

	@Override
	public Drawable getMapTile(MapTile tile) {
		Drawable rawTileDrawable = super.getMapTile(tile);
		Bitmap rawBitmap = drawableToBitmap(rawTileDrawable);
		if (rawBitmap != null) {
			Bitmap parsedBitmap = Bitmap.createBitmap(
					TILE_SIZE_PIXEL, TILE_SIZE_PIXEL, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(parsedBitmap);
			
			canvas.drawBitmap(rawBitmap, 0, 0, mPaint);
			
			Drawable parsedTileDrawable = bitmapToDrawable(parsedBitmap);
			
			return parsedTileDrawable;
		} else {
			return rawTileDrawable;
		}
	}

	public void setOpacity(int opacity) {
		int alpha;
		if (opacity < 0) {
			alpha = 0;
		} else if (0 <= opacity && opacity <= 100) {
			alpha = Math.round(opacity * 255 / 100);
		} else {
			alpha = 255;
		}
		
		mPaint.setAlpha(alpha);
	}

	private Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		
		Bitmap bitmap = null;
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			
			bitmap = bitmapDrawable.getBitmap();
		} else {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			if (width <= 0 || height <= 0) {
				bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
			} else {
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			}
			
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
		}
	    
	    return bitmap;
	}

	private Drawable bitmapToDrawable(Bitmap bitmap) {
		if (bitmap != null) {
			return new BitmapDrawable(mContext.getResources(), bitmap);
		} else {
			return null;
		}
	}

}
