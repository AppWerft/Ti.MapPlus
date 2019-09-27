/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.map;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.view.TiUIView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import ti.map.Shape.IShape;
import android.os.Message;

import android.app.Activity;

@Kroll.proxy(creatableInModule = MapModule.class, propertyAccessors = {
		TiC.PROPERTY_POSITION, MapModule.PROPERTY_PANNING,
		MapModule.PROPERTY_ZOOM, MapModule.PROPERTY_STREET_NAMES,
		MapModule.PROPERTY_USER_NAVIGATION })
public class StreetViewPanoramaProxy extends ViewProxy {
	public StreetViewPanoramaProxy() {
		super();
	}

	@Override
	public TiUIView createView(Activity activity) {
		return new TiStreetViewPanorama(this, activity);
	}
}
