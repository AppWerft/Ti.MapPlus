package ti.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TileProvider {
	private List<String> vNames = new ArrayList<String>();
	private JSONObject options;
	private JSONObject variants;
	private String url = "";
	private String ext = "";
	private int minZoom = 0;
	private int maxZoom = 19;
	private String tilematrixset = "";
	private String attribution = "";
	private String[] subdomains = {};
	public String LCAT = "TiMapFact";
	JSONObject provider;

	public String[] getVariantNames() {
		if (!provider.has("variants")) {
			return vNames.toArray(new String[vNames.size()]);
		} else {
			ArrayList<String> list = new ArrayList<String>();
			if (provider.has("variants")) {
				try {
					variants = provider.getJSONObject("variants");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Iterator<?> vkeys = variants.keys();
				while (vkeys.hasNext()) {
					String vkey = (String) vkeys.next();
					list.add(vkey);
				}
			}
			return list.toArray(new String[vNames.size()]);
		}
	}

	public KrollDict getVariant(String vName) {
		KrollDict variant = new KrollDict();
		variant.put("variant", "");
		if (vName == null) {
			//
		} else {
			variant.put("variant", vName);
			try {
				JSONObject variants = provider.getJSONObject("variants");
				Iterator<?> vkeys = variants.keys();
				while (vkeys.hasNext()) {
					String vkey = (String) vkeys.next();
					if (vkey.equals(vName)) {
						Object o = variants.get(vkey);
						if (o instanceof JSONObject) {
							JSONObject options = ((JSONObject) o)
									.getJSONObject("options");
							if (options.has("ext"))
								ext = options.getString("ext");
							if (options.has("url"))
								url = options.getString("url");
							if (options.has("variant"))
								variant.put("variant",
										options.getString("variant"));
							if (options.has("subdomains"))
								subdomains = getSubdomains(options
										.get("subdomains"));
							if (options.has("attribution"))
								attribution = options.getString("attribution");
							if (options.has("maxZoom"))
								maxZoom = options.getInt("maxZoom");
						} else
							variant.put("variant", (String) o);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		url = url.replace("{ext}", ext);
		url = url.replace("{variant}", variant.getString("variant"));
		url = url.replace("{tilematrixset}", tilematrixset);
		url = url.replace("{maxZoom}", "" + maxZoom);

		variant.remove("ext");
		variant.remove("variant");
		variant.put("attribution", attribution);
		variant.put("maxZoom", maxZoom);
		int length = subdomains.length;
		String[] endpoints;
		if (length > 0) {
			endpoints = new String[length - 1];
			for (int i = 1; i < subdomains.length; i++) {
				endpoints[i - 1] = url.replace("{s}", subdomains[i]);
			}
		} else {
			endpoints = new String[] { url };
		}
		variant.put("endpoints", endpoints);
		variant.put("endpoint", url);
		return variant;
	}

	private String[] getSubdomains(Object o) {
		if (o instanceof String) {
			String subs = (String) o;
			return subs.split("");
		} else if (o instanceof JSONArray) {
			JSONArray arr = (JSONArray) o;
			String[] list = new String[arr.length()];
			try {
				for (int i = 0; i < arr.length(); i++)
					list[i] = arr.getString(i);
			} catch (JSONException e) {
			}
			return list;

		} else
			return null; // default

	}

	private String getRandomUrl(KrollDict variant) {
		if (!variant.containsKey("subdomains"))
			return variant.getString("url");
		else {
			String[] subdomains = variant.getStringArray("variants");
			List<String> subdomainlist = Arrays.asList(subdomains);
			Collections.shuffle(subdomainlist);
			return subdomainlist.get(0);
		}
	}

	public TileProvider(JSONObject provider) {
		JSONObject options;
		if (provider == null)
			return;
		this.provider = provider;
		try {
			url = provider.getString("url");
		} catch (JSONException e) {
		}
		if (provider.has("options")) {
			try {
				options = provider.getJSONObject("options");
				if (options.has("ext"))
					ext = options.getString("ext");
				if (options.has("attribution"))
					attribution = options.getString("attribution");
				if (options.has("maxZoom"))
					maxZoom = options.getInt("maxZoom");
				if (options.has("tilematrixset"))
					tilematrixset = options.getString("tilematrixset");
				if (options.has("subdomains"))
					subdomains = getSubdomains(options.get("subdomains"));
				if (options.has("variant"))
					vNames.add(options.getString("variant"));

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
