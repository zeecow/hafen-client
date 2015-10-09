package haven;

import haven.RadarCFG.MarkerCFG;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Radar {
    public static final List<Marker> markers = new LinkedList<>();
    private static final MarkerCFG DEFAULT;
    public static final Comparator<Marker> MARKER_COMPARATOR = new Comparator<Marker>() {
	@Override
	public int compare(Marker o1, Marker o2) {
	    return o1.prio() - o2.prio();
	}
    };
    private static long lastsort = 0;

    static {
	DEFAULT = new DefMarker();
    }

    public static void add(Gob gob, Indir<Resource> res) {
	if(gob.getattr(Marker.class) == null) {
	    Marker marker = new Marker(gob, res);
	    gob.setattr(marker);
	    synchronized(markers) {
		markers.add(marker);
	    }
	}
    }

    private static MarkerCFG cfg(String resname) {
	if(resname == null) {
	    return null;
	}
	for(RadarCFG.Group group : RadarCFG.groups) {
	    for(MarkerCFG cfg : group.markerCFGs) {
		if(cfg.match(resname)) {
		    return cfg;
		}
	    }
	}
	return DEFAULT;
    }

    public static void tick() {
	long now = System.currentTimeMillis();
	if(now - lastsort > 100) {
	    synchronized(markers) {
		Collections.sort(markers, MARKER_COMPARATOR);
		lastsort = now;
	    }
	}
    }

    public static void remove(Gob gob) {
	if(gob != null) {
	    synchronized(markers) {
		markers.remove(gob.getattr(Marker.class));
		gob.delattr(Marker.class);
	    }
	}
    }

    public static class Marker extends GAttrib {
	private final Indir<Resource> res;
	private MarkerCFG cfg = null;
	private Tex tex;

	public Marker(Gob gob, Indir<Resource> res) {
	    super(gob);
	    this.res = res;
	}

	public Tex tex() {
	    if(tex == null && cfg() != null) {
		if(cfg == DEFAULT) {
		    GobIcon gi = gob.getattr(GobIcon.class);
		    if(gi != null) {
			tex = gi.tex();
		    }
		} else {
		    tex = cfg.tex();
		}
	    }
	    return tex;
	}

	public String tooltip() {
	    KinInfo ki = gob.getattr(KinInfo.class);
	    if(ki != null) {
		return ki.name;
	    } else if(cfg != null) {
		if(cfg.name != null) {
		    return cfg.name;
		} else {
		    return resname();
		}
	    }
	    return null;
	}

	public Color color() {
	    KinInfo ki = gob.getattr(KinInfo.class);
	    if(ki != null) {
		return BuddyWnd.gc[ki.group % BuddyWnd.gc.length];
//	    } else if(cfg != null && cfg.color != null) {
//		return cfg.color;
	    }
	    return Color.WHITE;
	}

	public int prio() {
	    return (cfg == null || tex == null) ? 0 : cfg.priority();
	}

	private MarkerCFG cfg() {
	    if(cfg == null) {
		cfg = Radar.cfg(resname());
	    }
	    return cfg;
	}

	private String resname() {
	    String name = null;
	    if(res instanceof Resource.Named) {
		name = ((Resource.Named) res).name;
	    } else {
		try {
		    name = res.get().name;
		} catch(Loading ignored) {
		}
	    }
	    return name;
	}
    }

    public static class DefMarker extends RadarCFG.MarkerCFG {
	@Override
	public Tex tex() {
	    return null;
	}

	@Override
	public int priority() {
	    return 0;
	}
    }

}
