/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import android.os.Bundle;
import android.os.Message;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

@Kroll.proxy(creatableInModule = MapModule.class, propertyAccessors = {
		MapModule.PROPERTY_POINTS, TiC.PROPERTY_COLOR, TiC.PROPERTY_WIDTH,
		TiC.PROPERTY_ANIMATED })
public class RouteProxy extends KrollProxy {
	final MarchingAnts marchingAnts = new MarchingAnts();

	/* private inner class for ant handeling */
	private final class PeriodicallyAntMarching extends TimerTask {

		@Override
		public void run() {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_UPDATE_ANTS), null);

		}
	}

	private PolylineOptions options;

	private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

	private static final int MSG_SET_POINTS = MSG_FIRST_ID + 400;
	private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
	private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;
	private static final int MSG_SET_PATTERN = MSG_FIRST_ID + 403;
	private static final int MSG_UPDATE_ANTS = MSG_FIRST_ID + 404;
	private static final int MSG_SET_ANIMATED = MSG_FIRST_ID + 405;
	private static final int MSG_STOP = MSG_FIRST_ID + 406;

	final String LCAT = MapModule.LCAT;

	public Polyline route;

	private Timer cron = new Timer();

	private boolean animated = false;

	public RouteProxy() {
		super();
	}

	@Override
	public boolean handleMessage(Message msg) {
		AsyncResult result = null;
		switch (msg.what) {
		case MSG_SET_POINTS: {
			result = (AsyncResult) msg.obj;
			route.setPoints(processPoints(result.getArg(), true));
			result.setResult(null);
			return true;
		}
		case MSG_SET_PATTERN: {
			result = (AsyncResult) msg.obj;
			try {
				List<PatternItem> pattern = processPattern(result.getArg());
				route.setPattern(pattern);
				result.setResult(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		case MSG_SET_COLOR: {
			result = (AsyncResult) msg.obj;
			route.setColor((Integer) result.getArg());
			result.setResult(null);
			return true;
		}

		case MSG_SET_WIDTH: {
			result = (AsyncResult) msg.obj;
			route.setWidth((Float) result.getArg());
			result.setResult(null);
			return true;
		}
		case MSG_UPDATE_ANTS:
			result = (AsyncResult) msg.obj;
			if (RouteProxy.this.route != null)
				RouteProxy.this.route.setPattern(marchingAnts.getNextPattern());
			result.setResult(null);
			return true;
		case MSG_SET_ANIMATED:
			result = (AsyncResult) msg.obj;
			animated = (boolean) (result.getArg());
			result.setResult(null);
			return true;
		default: {
			return super.handleMessage(msg);
		}
		}
	}

	public void initMarchingAnts() {
		if (animated) {
			int period = 50;
			int delay = 10;
			cron.scheduleAtFixedRate(new PeriodicallyAntMarching(), delay,
					period);
		}
	}

	public void processOptions() {
		options = new PolylineOptions();
		options.jointType(JointType.ROUND);
		if (hasProperty(TiC.PROPERTY_ANIMATED)) {
			animated = (TiConvert.toBoolean(getProperty(TiC.PROPERTY_ANIMATED)));
		}
		if (hasProperty(MapModule.PROPERTY_PATTERN) && !animated) {
			options.pattern(processPattern(getProperty(MapModule.PROPERTY_PATTERN)));
		}
		if (hasProperty(MapModule.PROPERTY_PATTERN)) {
			options.pattern(processPattern(getProperty(MapModule.PROPERTY_PATTERN)));
		}
		if (hasProperty(MapModule.PROPERTY_POINTS)) {
			processPoints(getProperty(MapModule.PROPERTY_POINTS), false);
		}
		if (hasProperty(TiC.PROPERTY_WIDTH)) {
			options.width(TiConvert.toFloat(getProperty(TiC.PROPERTY_WIDTH)));
		}
		if (hasProperty(TiC.PROPERTY_COLOR)) {
			options.color(TiConvert
					.toColor((String) getProperty(TiC.PROPERTY_COLOR)));
		}

	}

	public void addLocation(Object loc, ArrayList<LatLng> locationArray,
			boolean list) {
		if (loc instanceof HashMap) {
			HashMap<String, String> point = (HashMap<String, String>) loc;
			Object latitude = point.get(TiC.PROPERTY_LATITUDE);
			Object longitude = point.get(TiC.PROPERTY_LONGITUDE);
			if (longitude != null && latitude != null) {
				LatLng location = new LatLng(TiConvert.toDouble(latitude),
						TiConvert.toDouble(longitude));
				if (list) {
					locationArray.add(location);
				} else {
					options.add(location);
				}
			}
		}
	}

	public ArrayList<LatLng> processPoints(Object points, boolean list) {
		ArrayList<LatLng> locationArray = new ArrayList<LatLng>();
		// encoded (result from routing API)
		if (points instanceof String) {
			List<LatLng> locationList = PolyUtil.decode((String) points);
			return new ArrayList<LatLng>(locationList);
		}
		// multiple points
		if (points instanceof Object[]) {
			Object[] pointsArray = (Object[]) points;
			for (int i = 0; i < pointsArray.length; i++) {
				Object obj = pointsArray[i];
				addLocation(obj, locationArray, list);
			}
			return locationArray;
		}
		// single point
		addLocation(points, locationArray, list);
		return locationArray;
	}

	public List<PatternItem> processPattern(Object patternProxy) {
		List<PatternItem> patternItems = null;
		if (patternProxy instanceof PatternItemProxy) {
			patternItems = ((PatternItemProxy) patternProxy).getPatternItems();
			if (patternItems != null) {
				Log.d(LCAT, patternItems.toString());
				return patternItems;
			} else
				Log.e(LCAT, "patternItems was null");
		} else
			Log.e(LCAT,
					"patternItem is not really a patternItem, cannot add to map …");
		return patternItems;
	}

	public PolylineOptions getOptions() {
		return options;
	}

	public void setRoute(Polyline polyline) {
		route = polyline;
		if (route != null && animated == true)
			initMarchingAnts();
	}

	public Polyline getRoute() {
		return route;
	}

	@Override
	public void onPropertyChanged(String name, Object value) {
		super.onPropertyChanged(name, value);
		if (route == null) {
			return;
		}

		else if (name.equals(MapModule.PROPERTY_POINTS)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_POINTS), value);
		}

		else if (name.equals(MapModule.PROPERTY_PATTERN)) {
			Log.d(LCAT, "sendBlockingMainMessage");
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_PATTERN), value);

		} else if (name.equals(TiC.PROPERTY_COLOR)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_COLOR),
					TiConvert.toColor((String) value));
		}

		else if (name.equals(TiC.PROPERTY_WIDTH)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_WIDTH),
					TiConvert.toFloat(value));
		} else if (name.equals(TiC.PROPERTY_ANIMATED)) {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SET_ANIMATED),
					TiConvert.toBoolean(value));
		}

	}

	@Override
	public boolean hasProperty(String name) {
		return (super.getProperty(name) != null);
	}

	@Kroll.method
	public void stopAnimation() {
		Log.d(LCAT, "stopAnimation");
		/*
		 * if (cron != null) { Log.d(LCAT,"cron was active => kill");
		 * cron.cancel(); } TiMessenger.sendBlockingMainMessage(
		 * getMainHandler().obtainMessage(MSG_STOP), null);
		 */
	}
}
