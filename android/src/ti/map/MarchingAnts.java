package ti.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.appcelerator.kroll.common.Log;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.PatternItem;

public class MarchingAnts {
	private ArrayList<List<PatternItem>> marchingAnts;
	private int ndx = 0;
	private int length = 0;

	public MarchingAnts() {
		marchingAnts = new ArrayList<List<PatternItem>>();
		final PatternItem DASH = new Dash(5);
		final PatternItem GAP = new Gap(3);
		marchingAnts.add(Arrays.asList(GAP, DASH, DASH, DASH, DASH));
		marchingAnts.add(Arrays.asList(DASH, GAP, DASH, DASH, DASH));
		marchingAnts.add(Arrays.asList(DASH, DASH, GAP, DASH, DASH));
		marchingAnts.add(Arrays.asList(DASH, DASH, DASH, GAP, DASH));
		marchingAnts.add(Arrays.asList(DASH, DASH, DASH, DASH, GAP));
		length = marchingAnts.size();
	}

	public List<PatternItem> getNextPattern() {
		ndx = (ndx + 1) % length;
		return marchingAnts.get(ndx);
	}
}
