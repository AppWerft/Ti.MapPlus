/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONException;
import org.json.JSONObject;

import ti.map.Shape.IShape;
import android.graphics.Color;
import android.os.Message;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

@Kroll.proxy(name = "TileProviderFactory", creatableInModule = MapModule.class)
public class TileProviderFactoryProxy extends KrollProxy {
	private JSONObject Providers = null;
	final String LCAT = MapModule.LCAT;
	Map<String, TileProvider> providerList = new HashMap<String, TileProvider>();;

	public TileProviderFactoryProxy() {
		super();
		final String asset = "assets/TileProvider";
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			InputStream in = classLoader.getResourceAsStream(asset);
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			in.close();
			Providers = new JSONObject(new String(buffer, "UTF-8"));
			for (String providerName : getAllProviderNames()) {
				providerList
						.put(providerName,
								new TileProvider(Providers
										.getJSONObject(providerName)));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Kroll.method
	public String[] getProviderNames() {
		return getAllProviderNames();
	}

	@Kroll.method
	public String[] getAllProviderNames() {
		if (Providers == null)
			return null;
		ArrayList<String> list = new ArrayList<String>();
		Iterator<?> keys = Providers.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			try {
				if (Providers.get(key) instanceof JSONObject) {
					list.add(key);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list.toArray(new String[list.size()]);
	}

	@Kroll.method
	public String[] getAllVariantNamesByProvider(String pName) {
		return providerList.get(pName).getVariantNames();
	}

	@Kroll.method
	public String[] getVariantNamesByProvider(String pName) {
		return getAllVariantNamesByProvider(pName);
	}

	@Kroll.method
	public KrollDict getTileProvider(String endpoint) {
		if (endpoint.contains("/")) {
			String[] pair = endpoint.split("/");
			return providerList.get(pair[0]).getVariant(pair[1]);

		} else {
			return providerList.get(endpoint).getVariant(null);
		}
	}

	@Kroll.method
	public String getTileImage(KrollDict position) {
		double lat = 0f;
		double lng = 0f;
		int x = 0;
		int y = 0;
		String tileProvider;
		int zoom = 0;
		KrollDict kd = new KrollDict();
		KrollDict variant = new KrollDict();
		if (position.containsKeyAndNotNull("tileProvider")) {
			String endpoint = position.getString("tileProvider");
			if (endpoint.contains("/")) {
				String[] pair = endpoint.split("/");
				variant = providerList.get(pair[0]).getVariant(pair[1]);

			} else {
				variant = providerList.get(endpoint).getVariant(null);
			}

		}
		if (position.containsKeyAndNotNull("lat")
				&& position.containsKeyAndNotNull("lng")) {
			lat = position.getDouble("lat");
			lng = position.getDouble("lat");
			zoom = position.getInt("zoom");
			y = (int) (Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180)
					+ 1 / Math.cos(lat * Math.PI / 180))
					/ Math.PI)
					/ 2 * Math.pow(2, zoom)));
			x = (int) (Math.floor((lng + 180) / 360 * Math.pow(2, zoom)));
		}
		return variant.getString("endpoint").replace("{x}", "" + x)
				.replace("{y}", "" + y).replace("{z}", "" + zoom);
	}

	private String loadJSONFromAsset(String asset) {
		String json = null;
		try {
			InputStream inStream = TiFileFactory.createTitaniumFile(
					new String[] { asset }, false).getInputStream();
			byte[] buffer = new byte[inStream.available()];
			inStream.read(buffer);
			inStream.close();
			json = new String(buffer, "UTF-8");
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}
}
