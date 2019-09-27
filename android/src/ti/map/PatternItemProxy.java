/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.map;

import java.util.ArrayList;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.PatternItem;

@Kroll.proxy(creatableInModule = MapModule.class)
public class PatternItemProxy extends KrollProxy {
	private float strokeWidth = 5f;
	private float dashLength = 10f;
	private float gapLength = 10f;
	private int interval = 0;
	final String LCAT = MapModule.LCAT;
	private String patternString = ".";
	List<PatternItem> patternItems;

	public PatternItemProxy() {
		super();
	}

	public List<PatternItem> getPatternItems() {
		return patternItems;
	}

	public void setPatternItems(List<PatternItem> items) {
		patternItems = items;
	}

	@Override
	public void handleCreationDict(KrollDict opts) {
		if (opts.containsKeyAndNotNull(MapModule.PROPERTY_INTERVAL)) {
			interval = opts.getInt(MapModule.PROPERTY_INTERVAL);
		}
		if (opts.containsKeyAndNotNull("pattern")) {
			patternString = opts.getString("pattern");
		}
		if (opts.containsKeyAndNotNull(MapModule.PROPERTY_STROKE_WIDTH)) {
			strokeWidth = TiConvert.toFloat(opts
					.get(MapModule.PROPERTY_STROKE_WIDTH));
		}
		if (opts.containsKeyAndNotNull(MapModule.PROPERTY_DASH_LENGTH)) {
			try {
				dashLength = TiConvert.toFloat(opts
						.get(MapModule.PROPERTY_DASH_LENGTH));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (opts.containsKeyAndNotNull(MapModule.PROPERTY_GAP_LENGTH)) {
			try {
				gapLength = TiConvert.toFloat(opts
						.get(MapModule.PROPERTY_GAP_LENGTH));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		createPattern();

	}

	public void createPattern() {
		patternItems = new ArrayList<PatternItem>();
		for (String item : patternString.split("")) {
			switch (item) {
			case " ":
				patternItems.add(new Gap(gapLength));
				break;
			case ".":
				patternItems.add(new Dot());
				break;
			case "-":
				patternItems.add(new Dash(dashLength));
				break;
			}
		}
	}
}
