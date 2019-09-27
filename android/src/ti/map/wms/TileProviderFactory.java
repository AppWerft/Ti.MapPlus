package ti.map.wms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import android.util.Log;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
  public class TileProviderFactory {
  
      private static final String GEOSERVER_FORMAT =
            "http://yourApp.org/geoserver/wms" +
            "?service=WMS" +
            "&version=1.1.1" +              
            "&request=GetMap" +
            "&layers=yourLayer" +
            "&bbox=%f,%f,%f,%f" +
            "&width=256" +
            "&height=256" +
            "&srs=EPSG:900913" +
            "&format=image/png" +               
            "&transparent=true";    
    
    // return a geoserver wms tile layer
    private static TileProvider getTileProvider() {
        TileProvider tileProvider = new WMSTileProvider(256,256) {
            
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                String s = String.format(Locale.US, GEOSERVER_FORMAT, bbox[MINX], 
                        bbox[MINY], bbox[MAXX], bbox[MAXY]);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
        };
        return tileProvider;
      }
    
  }