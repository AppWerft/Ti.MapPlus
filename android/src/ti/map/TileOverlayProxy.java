/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.map;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import ti.map.wms.BoundingBox;

import com.cocoahero.android.gmaps.addons.mapbox.MapBoxOfflineTileProvider;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

@Kroll.proxy(creatableInModule = MapModule.class)
public class TileOverlayProxy extends KrollProxy {
	private TileOverlay tileOverlay;
	private TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();
	private float opacity = 1.0f;
	private int zIndex = 99;
	public String LCAT = MapModule.LCAT;
	public MapBoxOfflineTileProvider mbOfflineTileProvider;
	private static int TILE_WIDTH = 512;
	private static int TILE_HEIGHT = TILE_WIDTH;
	private KrollDict tileProviderParams;
	private String mbtiles;
	private boolean isWMS = false;
	private String WMSversion;
	private String WMSlayer;
	private String WMSurl;

	private String WMScrs = "UTM";
	private String WMSformat = "image/png";

	private final class UrlTileProviderHandler extends UrlTileProvider {
		private final String endpointOfTileProvider;

		private UrlTileProviderHandler(int w, int h,
				String endpointOfTileProvider) {
			super(w, h);
			this.endpointOfTileProvider = endpointOfTileProvider;
			// Log.d(LCAT, "endpointOfTileProvider=" + endpointOfTileProvider);
			// https://geodienste.hamburg.de/HH_WMS_Strassenbaumkataster?&BBOX=53.45,9.85,53.55,10.1&SERVICE=WMS&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&VERSION=1.3.0&LAYERS=strassenbaum_hpa&WIDTH=512&HEIGHT=512&CRS=EPSG%3A4326&STYLES=
		}

		@Override
		public synchronized URL getTileUrl(final int x, final int y,
				final int zoom) {
			URL tileUrl = null;
			String fUrl = null;
			BoundingBox bbox = new BoundingBox(x, y, zoom);
			if (isWMS == true) {
				fUrl = WMSurl.replace("{bbox}", bbox.getBBox_UTM()).replace(
						"{crs}", bbox.getCRS());
				Log.i(LCAT, "URL=" + fUrl);
				Log.i(LCAT, "LatLngBox=" + bbox.getBBox_WGS84());
				
				try {
					tileUrl = new URL(fUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else {
				// first the right tile depending on xyz
				fUrl = endpointOfTileProvider.replace("{z}", "" + zoom)
						.replace("{x}", "" + x).replace("{y}", "" + y);
				// loadbalancing:
				if (tileProviderParams.containsKey("subdomains")) {
					// same tile => same subdomain
					List<String> subdomainlist = Arrays
							.asList(tileProviderParams
									.getStringArray("subdomains"));
					int ndx = (x + y + zoom) % subdomainlist.size();
					// Collections.shuffle(subdomainlist);
					String subdomain = subdomainlist.get(ndx);
					fUrl = fUrl.replace("{s}", subdomain);
				}
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
				try {
					tileUrl = new URL(fUrl.replace("{s}", "a").replace(
							"{time	}", yyyyMMdd.format(cal.getTime())));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			return tileUrl;
		}
	}

	public TileOverlayProxy() {
		super();
	}

	// http://stackoverflow.com/questions/23806348/blurred-custom-tiles-on-android-maps-v2
	@Override
	public void handleCreationDict(KrollDict o) {
		super.handleCreationDict(o);
		String providerString = null;

		String url = "";
		if (o.containsKeyAndNotNull(TiC.PROPERTY_URL)) {
			url = o.getString(TiC.PROPERTY_URL);
		}
		if (o.containsKeyAndNotNull(TiC.PROPERTY_OPACITY)) {
			opacity = TiConvert.toFloat(o.getDouble(TiC.PROPERTY_OPACITY));
		}
		if (o.containsKeyAndNotNull(TiC.PROPERTY_ZINDEX)) {
			zIndex = o.getInt(TiC.PROPERTY_ZINDEX);
		}

		if (o.containsKeyAndNotNull(MapModule.PROPERTY_TYPE)) {
			if (o.getInt(MapModule.PROPERTY_TYPE) == MapModule.TYPE_WMS) {
				isWMS = true;
			}
		}
		if (o.containsKeyAndNotNull(MapModule.PROPERTY_MBTILES)) {
			mbtiles = o.getString(MapModule.PROPERTY_MBTILES);
		}
		if (o.containsKeyAndNotNull(MapModule.PROPERTY_VERSION)) {
			WMSversion = o.getString(MapModule.PROPERTY_VERSION);
		}
		if (o.containsKeyAndNotNull(MapModule.PROPERTY_LAYER)) {
			WMSlayer = o.getString(MapModule.PROPERTY_LAYER);
		}
		if (o.containsKeyAndNotNull("crs")) {
			WMScrs = o.getString("crs");
		}
		if (o.containsKeyAndNotNull("format")) {
			WMSformat = "image/" + o.getString("format");
		}

		if (o.containsKeyAndNotNull(MapModule.PROPERTY_TILE_PROVIDER)) {
			providerString = o.getString(MapModule.PROPERTY_TILE_PROVIDER);
			TileProviderFactoryProxy providerList = new TileProviderFactoryProxy();
			tileProviderParams = providerList.getTileProvider(providerString);
		}
		if (isWMS) {
			WMSurl = url + "?SERVICE=WMS&VERSION=" + WMSversion
					+ "&REQUEST=GetMap&FORMAT=" + WMSformat
					+ "&TRANSPARENT=true&LAYERS=" + WMSlayer + "&WIDTH="
					+ TILE_WIDTH + "&HEIGHT=" + TILE_HEIGHT
					+ "&SRS={crs}&STYLES=&BBOX={bbox}&CRS={crs}&TILED=true";
		}
		if (providerString == null && mbtiles == null && isWMS == false) {
			Log.e(LCAT, "no mbtiles, no tileProvider");
		} else {
			TileProvider tileProvider = null;
			if (isWMS) {
				/*
				 * WMPS Provider
				 */
				if (WMSformat.equals("image/png"))
					tileProvider = new UrlTileProviderHandler(TILE_WIDTH,
							TILE_HEIGHT, WMSurl);
				else
					tileProvider = new SVGTileProvider(
							new UrlTileProviderHandler(TILE_WIDTH, TILE_HEIGHT,
									WMSurl));
				/*
				 * Standard TMS Provider like OSM Using of Canvas because of bad
				 * resolution of tiles
				 */
			} else if (tileProviderParams.containsKey("endpoint")) {
				tileProvider = new CanvasTileProvider(
						new UrlTileProviderHandler(TILE_WIDTH, TILE_HEIGHT,
								tileProviderParams.getString("endpoint")));

				/* offline maps (MBtiles) */
			} else if (mbtiles != null) {
				File mbtilesFile = new File(mbtiles.replace("file://", ""));
				if (mbtilesFile.exists()) {
					MapBoxOfflineTileProvider mbOfflineTileProvider = new MapBoxOfflineTileProvider(
							mbtilesFile);
					tileOverlayOptions.tileProvider(mbOfflineTileProvider);
				} else
					Log.e("LCAT", "mb file not found " + mbtilesFile);
			}

			if (tileProvider != null) {
				tileOverlayOptions.tileProvider(tileProvider)
						.transparency(1.0f - opacity).zIndex(zIndex);

			} else {
				Log.e(LCAT, "no tileProvider available");

			}
		}
	}

	public TileOverlayOptions getOptions() {
		return tileOverlayOptions;
	}

	public TileOverlay getTileOverlay() {
		return tileOverlay;
	}

	@Kroll.method
	public void destroy() {
		if (mbOfflineTileProvider != null) {
			mbOfflineTileProvider.close();
		}
	}

	public void setTileOverlay(TileOverlay tileOverlay) {
		this.tileOverlay = tileOverlay;
	}

}
