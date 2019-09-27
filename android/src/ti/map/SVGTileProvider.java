package ti.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;
import com.larvalabs.svgandroid.SVGParseException;

public class SVGTileProvider implements TileProvider {
	static final int TILE_SIZE = 512;
	private TileProvider tileProvider;
	Paint paint = new Paint();
	static float DPI = 72.0f;   // Should be settable

	public SVGTileProvider(TileProvider tileProvider) {
		this.tileProvider = tileProvider;
	}
	
	@Override
	public Tile getTile(int x, int y, int zoom) {
		byte[] data=null;
		Bitmap image = getNewBitmap();
		// image is now an emmpty PNG
		Canvas canvas = new Canvas(image);
		boolean isOk = onDraw(canvas, zoom, x, y);
		image.recycle();
		return new Tile(zoom, zoom, data);
	}

	private boolean onDraw(Canvas canvas, int zoom, int x, int y) {
		Tile tile = tileProvider.getTile(x, y, zoom);
		if (tile == NO_TILE) {
			return false;
		}
		if (tile != NO_TILE) {
			SVGBuilder builder = new SVGBuilder();
			builder.readFromString(new String(tile.data));
			SVG svg = builder.build();
			svg.getPicture().draw(canvas);
		}
		return true;
	}
	/**
     * Parse SVG data from a string.
     *
     * @param svgData the string containing SVG XML data.
     * @return the parsed SVG.
     * @throws SVGParseException if there is an error while parsing.
     */
    
	/*generates an emty transparent image*/
	private Bitmap getNewBitmap() {
		Bitmap image = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
				Bitmap.Config.ARGB_8888);
		image.eraseColor(Color.TRANSPARENT);
		return image;
	}

	
}
