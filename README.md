# Titanium Map.Overlays Module 

This is the Map Module for Titanium extended by TileOverlays. 


<img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/watercolor.png" width=240 /> <img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/mapstyles.png" width=240 /> <img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/Screenshot_20170219-112805.png" width=240 /> 
<img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/Screenshot_20170219-114755.png" width=240 /> <img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/osmsea.png" width=240 />


## Usage

### Using of TileOverlays

The module extends appcelerator's ti.map with raster based overlays in wgs84 (web mercator, epsg:4326) projection.
A couple of formats are supported.

* XYZ – Slippy map (TMS, OSM, mapbox, Google, MapQuest …)
* WMS
* WMTS


### XYZ

The most tile providers uses this simple format. You can use this syntax:

```
Map.addOverlay(Map.createTileLayer({
	xyz : {
	   url : 'http://stamen-tiles-a.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.png',
	   toupper : true  // optional id y axis is counted from bottom to to
	}
));
```
"XYZ" is the term we use for various ad hoc schemes for serving tiles, often from a static file system. There's no standard and no mechanism for metadata. There's no fixed standard for Y axis direction although down from the top is most common. Google did something for their own purposes, and other people sort of copied it. With parameter `toupper` you can control it.


#### Usage of symbolic names
You can use symbolic names for tiles. To get all available tiles:

```javascript
var providerList = Ti.Map.createTileProviderFactory();
providerList.getAllProviderNames(); 
// ["OpenStreetMap","OpenSeaMap","OpenTopoMap","Thunderforest","OpenMapSurfer","Hydda","MapBox","Stamen","Esri","OpenWeatherMap","FreeMapSK","MtbMap","CartoDB","HikeBike","BasemapAT","NASAGIBS","NLS"]

var variants = factory.getAllVariantNamesByProvider("Stamen");  // gives list of all variants
//  ["Toner","TonerBackground","TonerHybrid","TonerLines","TonerLabels","TonerLite","Watercolor","Terrain","TerrainBackground","TopOSMRelief","TopOSMFeatures"]
```

With knowledge of type name you can call:

```
Map.addTileOverlay(Map.createTileLayer({
	xyz : {
		type : 'Stamen/Watercolor',
		apikey : "fgsuna73hdfod72h"  // optional (mapbox)
	}
));
```


### WMS

WMS is a protocol defined by the OGC for requesting rendered map images for arbitrary areas. Clients can make requests to it in a tiled pattern if they wish.

```
Map.addOverlay(Map.createTileLayer({
	wms : {
		url :'https://geodienste.hamburg.de/HH_WMS_Strassenbaumkataster',
		version : "1.3.0",
		format : 'png',
		transparent : true,
		layers : ['strassenbaum'],
		styles : ['a',‘b‘], // optional
		crs : 'epsg:4326',  // optional
		tiles : true
}));
```

### WMTS

WMTS is a protocol created by the OGC that allows tiles to be requested using integer indices. It also provides metadata for discovery and configuration. Y axis goes down from the top.

```
Map.addOverlay(Map.createTileLayer({
	wtms : {
		url : 'http://wxs.ign.fr/choisirgeoportail/geoportail/wmts',
		version : '1.1.0',
		tilematrixset : 'PM',
		layer : 'GEOGRAPHICALGRIDSYSTEMS.MAPS',
		format : 'image/jpeg',
		style : 'normal'		
	}
}));
```

### Getting static tiles
```javascript
Ti.UI.createImageView({
    width : 256,
    height : 256,
    image : Ti.Map.createTileProviderFactory().getTileImage({
        tileProvider : "Stamen/WaterColor"
        lat : 53.55,
        lng : 10.01,
        zoom : 12
    })
});
```

### Offline tiles

With the [Perl script](http://search.cpan.org/~rotkraut/Geo-OSM-Tiles-0.01/downloadosmtiles.pl) you can download all tiles from a region. This script generates folders and download all. After this you can use [mbutil](https://github.com/mapbox/mbutil/) for converting in mbtiles format. This sqlite format is basic for offline maps. Now you can call:
```javascript
var offlineOverlay =  Ti.Map.createTileOverlay({
   	mbtiles : Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory,"germany.mbtiles").nativePath,
});
mapView.addOverlay(offlineOverlay);
```

You can use this module for display deep zoom images:
```javascript
var imageOverlay =  Ti.Map.createTileOverlay({
	url : "https://raw.githubusercontent.com/alfarisi/leaflet-deepzoom/master/example/DeepZoomImage/hubble_files/{z}/{x}_{y}.jpg"
});
 ```
You can create images with [zoomify](https://www.macupdate.com/app/mac/58319/zoomify/download).
 
Microsofts [DeepzoommImages](https://en.wikipedia.org/wiki/Deep_Zoom) will currently  not supported. 

Because the offline Maps work with sqlite database you have to close the connection after map work:

```javascript
offlineOverlay.destroy();
```
This prevent memory leaks!


### Usage of WMS (Web Map Service)
A Web Map Service (WMS) is a standard protocol for serving (over the Internet) georeferenced map images which a map server generates using data from a GIS database.[3] The Open Geospatial Consortium developed the specification and first published it in 1999.

```javascript
Ti.Map = require("ti.map");
var mapView = Ti.Map.createView();
var TreesOverlay = Ti.Map.createTileOverlay({
	type : Ti.Map.TYPE_WMS,
	url : "https://geodienste.hamburg.de/	HH_WMS_Strassenbaumkataster",
	layer : "strassenbaum",
	version: "1.3.0"
})
mapView.addtileOverlay(TreesOverlay);
```
and result:

<img src="https://lh3.googleusercontent.com/vlQeuCdmYAaCTAjpBwEo1x68gAUNbRz6iWx2OpyElPCXXGu1lnbRI8QCB5mlFWZ5iUn-=h900" width=320/>

This only works  if geoserver supports Universal Transverse Mercator coordinate system (UTM).


### SVG tiles from WMS
If you prefere svg tiles (for performance reasons) the you can use `svg` as value for format property. Attention! Geoserver must suppport this output. Although your getCapability request announced this format (image/svg) it is no save. This feature is still in progress.

```javascript
Ti.Map = require("ti.map");
var mapView = Ti.Map.createView();
var TreesOverlay = Ti.Map.createTileOverlay({
	type : Ti.Map.TYPE_WMS,
	url : "https://geodienste.hamburg.de/HH_WMS_Strassenbaumkataster",
	layer : "strassenbaum",
	style : "style_strassenbaum"
	width : 256, //default
	height : 256, //default
	version: "1.3.0",
	format : "svg",
	dpi : 72 // default
})
mapView.addtileOverlay(TreesOverlay);
```


## Autorotating (following device bearing)

```javascript
mapView.startRotate();
// later, if window blurred:
mapView.stopRotate();
```

## Heatmaps
Heatmaps are useful for representing the distribution and density of data points on a map.
<img src="https://i.stack.imgur.com/FkVco.jpg" width=400 />

```javascript
var heatMap  = Ti.Map.createHeatmapOverlay({
    points : [
        {"lat" : -37.1886, "lng" : 145.708 },
        {"lat" : -37.8361, "lng" : 144.845 },
        {"lat" : -38.4034, "lng" : 144.192 },
        {"lat" : -38.7597, "lng" : 143.67 },
        {"lat" : -36.9672, "lng" : 141.083 }
        ],
    opacity : 0.9, // optionally
    gradient : {   // optionally
        colors : ["#ff0000","#0000ff"],
        startPoints : [0.2,1.0]
    },
    radius : 20 //default, range is 10…50
});
mapView.addHeatmapOverlay(heatMap);
heatMap.setPoints(/* new data */);
```
### Use weighted latitude/longitude points

When creating a HeatmapTileProvider, you can pass it a collection of weighted latitude/longitude coordinates. This is useful if you want to illustrate the importance of a particular set of locations.

```javascript
points : [
        {"lat" : -37.1886, "lng" : 145.708 ,intensity: 0.2},
        {"lat" : -37.8361, "lng" : 144.845  ,intensity: 0.4},
        {"lat" : -38.4034, "lng" : 144.192  ,intensity: 2.0},
        {"lat" : -38.7597, "lng" : 143.67  ,intensity: 0.25},
        {"lat" : -36.9672, "lng" : 141.083  ,intensity: 0.02}
        ],
```

### Heatmaps with large data set

Alternativly to points parameter you can use a sqlite database. The result set of SELECT query must contains `latitude` and  `longitude` in this order! The importer reads first and second parameter.

```javasscript
var heatMap  = Ti.Map.createHeatmapOverlay({
    dbname : "trees",
    select : "SELECT latitude,longitude from treestable",
});
mapView.addHeatmapOverlay(heatMap);

```


## Using of encoded polylines

The Ti.Map.createRoute() point property accepts now encoded polylines.

```javascript
Ti.Map.createRoute({
	points : "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
	color : "#8f00",
	width: 5
});
```

## Pattern in routes (dotted, dashed …)

<img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/dotted.png" width=400 />

```javascript
var patternItem = Ti.Map.createPatternItem({
	dashLength : 20,
	gapLength :20,
	pattern : "-"  // dashed line
});
mapView.addRoute(Ti.Map.createRoute({
	points : "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
	patternItem : patternItem,
	color : "red",
	jointType : Ti.Map.JOINT_TYPE_BEVEL, // JOINT_TYPE_BEVEL,JOINT_TYPE_ROUND, JOINT_TYPE_DEFAULT,
	with : 5,
}));
mapView.addRoute(Ti.Map.createRoute({
	points : "_pa1e3wf~iF~pstzadasdalLnnqC_mqNvxq`@",
	patternItem : Ti.Map.createPatternItem({
		pattern : "."  // dotted line
	}),
	color : "orange",
	with : 5,
}));
```

### Animated routes ("marching ants")

```javascript
var Route = Ti.Map.createRoute({
	points : "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
	color : "red",
	animated : true, 
	with : 5,
});
mapView.addRoute(Route);
```

<img src="https://raw.githubusercontent.com/AppWerft/Ti.MapPlus/master/screens/ants.gif" width=400/>





