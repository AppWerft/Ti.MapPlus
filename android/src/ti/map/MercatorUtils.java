package ti.map;

public class MercatorUtils {

	/* global define, window */
	/*
	 * Global Map Tiles as defined in Tile Map Service (TMS) Profiles
	 * ============================================================== Functions
	 * necessary for generation of global tiles used on the web. It contains
	 * classes implementing coordinate conversions for: - GlobalMercator (based
	 * on EPSG:900913 = EPSG:3785) for Google Maps, Yahoo Maps, Microsoft Maps
	 * compatible tiles - GlobalGeodetic (based on EPSG:4326) for OpenLayers
	 * Base Map and Google Earth compatible tiles More info at:
	 * http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification
	 * http://wiki.osgeo.org/wiki/WMS_Tiling_Client_Recommendation
	 * http://msdn.microsoft.com/en-us/library/bb259689.aspx
	 * http://code.google.com
	 * /apis/maps/documentation/overlays.html#Google_Maps_Coordinates Created by
	 * Klokan Petr Pridal on 2008-07-03. Google Summer of Code 2008, project
	 * GDAL2Tiles for OSGEO. In case you use this class in your product,
	 * translate it to another language or find it usefull for your project
	 * please let me know. My email: klokan at klokan dot cz. I would like to
	 * know where it was used. Class is available under the open-source GDAL
	 * license (www.gdal.org). """ import math class GlobalMercator(object): """
	 * TMS Global Mercator Profile --------------------------- Functions
	 * necessary for generation of tiles in Spherical Mercator projection,
	 * EPSG:900913 (EPSG:gOOglE, Google Maps Global Mercator), EPSG:3785,
	 * OSGEO:41001. Such tiles are compatible with Google Maps, Microsoft
	 * Virtual Earth, Yahoo Maps, UK Ordnance Survey OpenSpace API, ... and you
	 * can overlay them on top of base maps of those web mapping applications.
	 * 
	 * Pixel and tile coordinates are in TMS notation (origin [0,0] in
	 * bottom-left). What coordinate conversions do we need for TMS Global
	 * Mercator tiles:: LatLon <-> Meters <-> Pixels <-> Tile WGS84 coordinates
	 * Spherical Mercator Pixels in pyramid Tiles in pyramid lat/lon XY in
	 * metres XY pixels Z zoom XYZ from TMS EPSG:4326 EPSG:900913 .----.
	 * --------- -- TMS / \ <-> | | <-> /----/ <-> Google \ / | | /--------/
	 * QuadTree ----- --------- /------------/ KML, public WebMapService Web
	 * Clients TileMapService What is the coordinate extent of Earth in
	 * EPSG:900913? [-20037508.342789244, -20037508.342789244,
	 * 20037508.342789244, 20037508.342789244] Constant 20037508.342789244 comes
	 * from the circumference of the Earth in meters, which is 40 thousand
	 * kilometers, the coordinate origin is in the middle of extent. In fact you
	 * can calculate the constant as: 2 * math.pi * 6378137 / 2.0 $ echo 180 85
	 * | gdaltransform -s_srs EPSG:4326 -t_srs EPSG:900913 Polar areas with
	 * abs(latitude) bigger then 85.05112878 are clipped off. What are zoom
	 * level constants (pixels/meter) for pyramid with EPSG:900913? whole region
	 * is on top of pyramid (zoom=0) covered by 256x256 pixels tile, every lower
	 * zoom level resolution is always divided by two INITIALRESOLUTION =
	 * 20037508.342789244 * 2 / 256 = 156543.03392804062 What is the difference
	 * between TMS and Google Maps/QuadTree tile name convention? The tile
	 * raster itself is the same (equal extent, projection, pixel size), there
	 * is just different identification of the same raster tile. Tiles in TMS
	 * are counted from [0,0] in the bottom-left corner, id is XYZ. Google
	 * placed the origin [0,0] to the top-left corner, reference is XYZ.
	 * Microsoft is referencing tiles by a QuadTree name, defined on the
	 * website: http://msdn2.microsoft.com/en-us/library/bb259689.aspx The
	 * lat/lon coordinates are using WGS84 datum, yeh? Yes, all lat/lon we are
	 * mentioning should use WGS84 Geodetic Datum. Well, the web clients like
	 * Google Maps are projecting those coordinates by Spherical Mercator, so in
	 * fact lat/lon coordinates on sphere are treated as if the were on the
	 * WGS84 ellipsoid.
	 * 
	 * From MSDN documentation: To simplify the calculations, we use the
	 * spherical form of projection, not the ellipsoidal form. Since the
	 * projection is used only for map display, and not for displaying numeric
	 * coordinates, we don't need the extra precision of an ellipsoidal
	 * projection. The spherical projection causes approximately 0.33 percent
	 * scale distortion in the Y direction, which is not visually noticable. How
	 * do I create a raster in EPSG:900913 and convert coordinates with PROJ.4?
	 * You can use standard GIS tools like gdalwarp, cs2cs or gdaltransform. All
	 * of the tools supports -t_srs 'epsg:900913'. For other GIS programs check
	 * the exact definition of the projection: More info at
	 * http://spatialreference.org/ref/user/google-projection/ The same
	 * projection is degined as EPSG:3785. WKT definition is in the official
	 * EPSG database. Proj4 Text: +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0
	 * +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs Human
	 * readable WKT format of EPGS:900913: PROJCS["Google Maps Global Mercator",
	 * GEOGCS["WGS 84", DATUM["WGS_1984",
	 * SPHEROID["WGS 84",6378137,298.2572235630016, AUTHORITY["EPSG","7030"]],
	 * AUTHORITY["EPSG","6326"]], PRIMEM["Greenwich",0],
	 * UNIT["degree",0.0174532925199433], AUTHORITY["EPSG","4326"]],
	 * PROJECTION["Mercator_1SP"], PARAMETER["central_meridian",0],
	 * PARAMETER["scale_factor",1], PARAMETER["false_easting",0],
	 * PARAMETER["false_northing",0], UNIT["metre",1, AUTHORITY["EPSG","9001"]]]
	 */

	final static int TILESIZE = 256;
	final static double INITIALRESOLUTION = 2 * Math.PI * 6378137 / TILESIZE;
	final static double ORIGINSHIFT = Math.PI * 6378137;

	// Resolution (meters/pixel) for given zoom level (measured at Equator)
	static double resolution(int zoom) {
		return INITIALRESOLUTION / Math.pow(2, zoom);
	}

	// Zoom level for given resolution (measured at Equator)
	static double zoom(double resolution) {
		return Math.round(Math.log(INITIALRESOLUTION / resolution)
				/ Math.log(2));
	}

	// Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator
	// EPSG:900913
	static double[] latLonToMeters(double lat, double lon) {
		double mx = lon * ORIGINSHIFT / 180;
		double my = Math.log(Math.tan((90 + lat) * Math.PI / 360))
				/ (Math.PI / 180);
		my = my * ORIGINSHIFT / 180;
		double[] res = { mx, my };
		return res;
	}

	// Converts pixel coordinates in given zoom level of pyramid to EPSG:900913
	static double[] pixelsToMeters(double px, double py, int zoom) {
		double res = resolution(zoom);
		double x = px * res - ORIGINSHIFT;
		double y = py * res - ORIGINSHIFT;
		double[] ret = { x, y };
		return ret;
	}

	// Converts EPSG:900913 to pixel coordinates in given zoom level
	static double[] metersToPixels(double mx, double my, int zoom) {
		double res = resolution(zoom);

		double[] ret = { (mx + ORIGINSHIFT) / res, (my + ORIGINSHIFT) / res };
		return ret;
	}

	// Converts given lat/lon in WGS84 Datum to pixel coordinates in given zoom
	// level
	static double[] latLonToPixels(double lat, double lon, int zoom) {
		double[] meters = new double[2];
		meters = latLonToMeters(lat, lon);
		return metersToPixels(meters[0], meters[1], zoom);
	}

	// Converts pixel coordinates in given zoom level to lat/lon in WGS84 Datum
	static double[] pixelsToLatLon(double px, double py, int zoom) {
		double[] meters = pixelsToMeters(px, py, zoom);
		return metersToLatLon(meters[0], meters[1]);
	}

	// Returns a tile covering region in given pixel coordinates
	static double[] pixelsToTile(double px, double py) {
		double[] ret = new double[2];
		ret[0] = Math.floor(px / TILESIZE);
		ret[1] = Math.floor(py / TILESIZE);
		return ret;
	}

	// Returns tile for given mercator coordinates
	static double[] metersToTile(double mx, double my, int zoom) {
		double[] pixels = metersToPixels(mx, my, zoom);
		return pixelsToTile(pixels[0], pixels[1]);
	}

	static double[] tilePixelBounds(double tx, double ty, int zoom) {
		double[] bounds = tileBounds(tx, ty, zoom);
		double[] min = metersToPixels(bounds[0], bounds[1], zoom);
		double[] max = metersToPixels(bounds[2], bounds[3], zoom);
		double[] ret = new double[4];
		System.arraycopy(min, 0, ret, 0, 2);
		System.arraycopy(max, 0, ret, 2, 2);
		return ret;
	}

	// Returns bounds of the given tile in EPSG:900913 coordinates
	static double[] tileBounds(double tx, double ty, int zoom) {
		double[] min = pixelsToMeters(tx * TILESIZE, ty * TILESIZE, zoom);
		double[] max = pixelsToMeters((tx + 1) * TILESIZE, (ty + 1) * TILESIZE,
				zoom);
		double[] ret = new double[4];
		System.arraycopy(min, 0, ret, 0, 2);
		System.arraycopy(max, 0, ret, 2, 2);

		return ret;
	}

	// Returns bounds of the given tile in latutude/longitude using WGS84 datum
	static double[] tileLatLonBounds(double tx, double ty, int zoom) {
		double[] bounds = tileBounds(tx, ty, zoom);
		double[] min = metersToLatLon(bounds[0], bounds[1]);
		double[] max = metersToLatLon(bounds[2], bounds[3]);
		double[] ret = new double[4];
		System.arraycopy(min, 0, ret, 0, 2);
		System.arraycopy(max, 0, ret, 2, 2);
		return ret;
	}
	
	static String tileWebMercatorBBox(double tx, double ty, int zoom) {
		// EPSG:3857
		double[] bounds = tileBounds(tx, ty, zoom);
		return String.valueOf(bounds[0]) + "," + String.valueOf(-bounds[3]) + ","
		+ String.valueOf(bounds[2]) + "," + String.valueOf(-bounds[1]);
	}
	
	
	static String TMStoWMS(int tx, int ty, int zoom, String crs) {
		double[] bbox = tileLatLonBounds(tx, ty, zoom);
		if (!crs.equals("EPSG:4326")) {
			//CoordinateReferenceSystem sourceCRS = CRS.forCode("EPSG:4326");
		}
		return String.valueOf(bbox[3]) + "," + String.valueOf(bbox[0]) + ","
				+ String.valueOf(bbox[1]) + "," + String.valueOf(bbox[2]);
	}

	// Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84
	// Datum
	static double[] metersToLatLon(double mx, double my) {
		double lon = (mx / ORIGINSHIFT) * 180;
		double lat = (my / ORIGINSHIFT) * 180;
		lat = -180 / Math.PI
				* (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
		double[] res = new double[2];
		res[0] = lon;
		res[1] = lat;
		return res;
	}
}
