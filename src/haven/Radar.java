package haven;

import haven.RadarCFG.MarkerCFG;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

import static haven.MCache.*;

public class Radar {
    public static final List<Marker> markers = new LinkedList<>();
    public static final List<Queued> queue = new LinkedList<>();
    public static final Map<String, MarkerCFG> cfg_cache = new HashMap<>();
    private static final MarkerCFG DEFAULT = new DefMarker();
    public static final Comparator<Marker> MARKER_COMPARATOR = Comparator.comparingInt(Marker::prio);
    private static long lastsort = 0;
    private static boolean needSorting = false;
    
    public static void add(Gob gob, Indir<Resource> res) {
	if(gob.getattr(Marker.class) == null) {
	    synchronized (queue) {
		if(queue.stream().noneMatch(q -> q.gob.id == gob.id)) {
		    queue.add(new Queued(gob, res));
		}
	    }
	}
    }
    
    public static void add(Gob gob) {
	Indir<Resource> res = gob.getires();
	if(res != null) {
	    add(gob, res);
	}
    }
    
    private static MarkerCFG cfg(String resname) {
	MarkerCFG result = cfg_cache.get(resname);
	if(result == null) {
	    result = DEFAULT;
	    outer:
	    for (RadarCFG.Group group : RadarCFG.groups) {
		for (MarkerCFG cfg : group.markerCFGs) {
		    if(cfg.match(resname)) {
			result = cfg;
			break outer;
		    }
		}
	    }
	    cfg_cache.put(resname, result);
	}
	return result;
    }
    
    public static void tick() {
	synchronized (queue) {
	    Iterator<Queued> iterator = queue.iterator();
	    while (iterator.hasNext()) {
		Queued queued = iterator.next();
		if(queued.ready()) {
		    String resname = queued.resname();
		    MarkerCFG cfg = cfg(resname);
		    Gob gob = queued.gob;
		    if(cfg != DEFAULT || gob.getattr(GobIcon.class) != null) {
			Marker marker = new Marker(gob, resname);
			synchronized (markers) {
			    markers.add(marker);
			}
			gob.setattr(marker);
			needSorting = true;
		    }
		    iterator.remove();
		}
	    }
	}
	
	long now = System.currentTimeMillis();
	if(needSorting && now - lastsort > 100) {
	    synchronized (markers) {
		markers.sort(MARKER_COMPARATOR);
		lastsort = now;
		needSorting = false;
	    }
	}
    }
    
    public static void remove(long id) {
	synchronized (markers) {
	    markers.removeIf(m -> m.gob.id == id);
	}
	synchronized (queue) {
	    queue.removeIf(q -> q.gob.id == id);
	}
    }
    
    public static final Coord VIEW_SZ = MCache.sgridsz.mul(9).div(tilesz.floor());// view radius is 9x9 "server" grids
    public static final Color VIEW_BG_COLOR = new Color(255, 255, 255, 60);
    public static final Color VIEW_BORDER_COLOR = new Color(0, 0, 0, 128);
    
    public static void draw(GOut g, Function<Coord2d, Coord> transform, Coord2d player, int scale) {
	if(CFG.MMAP_VIEW.get() && player != null) {
	    Coord2d sgridsz = new Coord2d(MCache.sgridsz);
	    Coord rc = transform.apply(player.div(sgridsz).floor().sub(4, 4).mul(sgridsz));
	    if(rc != null) {
		g.chcolor(VIEW_BG_COLOR);
		g.frect(rc, VIEW_SZ.div(scale));
		g.chcolor(VIEW_BORDER_COLOR);
		g.rect(rc, VIEW_SZ.div(scale));
		g.chcolor();
	    }
	}
	try {
	    List<Marker> marks = safeMarkers();
	    for (Marker marker : marks) {
		try {
		    Tex tex = marker.tex();
		    if(tex != null) {
			Coord coord = transform.apply(marker.gob.rc);
			if(coord != null) {
			    if(marker.colored) {g.chcolor(marker.color());}
			    g.image(tex, coord.sub(tex.sz().div(2)));
			    if(marker.colored) {g.chcolor();}
			}
		    }
		} catch (Loading ignored) {}
	    }
	} catch (Exception ignored) {}
    }
    
    public static List<Marker> safeMarkers() {
	List<Marker> marks;
	synchronized (markers) {
	    marks = new ArrayList<>(markers);
	}
	return marks;
    }
    
    public static void clean() {
	synchronized (markers) {
	    markers.clear();
	}
    }
    
    public static class Marker extends GAttrib {
	private final String resname;
	private MarkerCFG cfg;
	private Tex tex;
	private boolean colored = false;
	
	public Marker(Gob gob, String res) {
	    super(gob);
	    this.resname = res;
	    cfg = cfg(resname);
	}
	
	public Tex tex() {
	    if(!cfg.visible()) {
		return null;
	    }
	    if(tex == null) {
		if(cfg == DEFAULT || cfg.icon == null) {
		    GobIcon gi = gob.getattr(GobIcon.class);
		    if(gi != null) {
			tex = gi.tex();
		    } else if(cfg.parent != null) {
			tex = cfg.parent.tex();
			colored = true;
		    }
		} else {
		    tex = cfg.tex();
		    colored = cfg.icon.charAt(0) == '$';
		}
	    }
	    return tex;
	}
	
	public String tooltip(boolean full) {
	    KinInfo ki = gob.getattr(KinInfo.class);
	    if(full) {
		return resname;
	    } else if(ki != null) {
		return ki.name;
	    } else if(cfg != null)
		if(Boolean.TRUE.equals(cfg.resname)) {
		    return cfg.name != null ? String.format("%s (%s)", cfg.name, pretty(resname)) : resname;
		} else if(cfg.name != null) {
		    return cfg.name;
		}
	    return pretty(resname);
	}
	
	private static String pretty(String name) {
	    int k = name.lastIndexOf("/");
	    name = name.substring(k + 1);
	    name = name.substring(0, 1).toUpperCase() + name.substring(1);
	    return name;
	}
	
	public Color color() {
	    KinInfo ki = gob.getattr(KinInfo.class);
	    if(ki != null) {
		return BuddyWnd.gc[ki.group % BuddyWnd.gc.length];
	    } else if(colored) {
		return cfg.color();
	    }
	    return Color.WHITE;
	}
	
	public boolean isDefault() {
	    return cfg == DEFAULT;
	}
	
	public int prio() {
	    return (tex == null) ? 0 : cfg.priority();
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
	
	@Override
	public boolean visible() {
	    return true;
	}
    }
    
    private static class Queued {
	public final Gob gob;
	public final Indir<Resource> res;
	
	public Queued(Gob gob, Indir<Resource> res) {
	    this.gob = gob;
	    this.res = res;
	}
	
	public boolean ready() {
	    boolean ready = true;
	    try {
		resname();
	    } catch (Loading e) {
		ready = false;
	    }
	    return ready;
	}
	
	public String resname() {
	    String name;
	    if(res instanceof Resource.Named) {
		name = ((Resource.Named) res).name;
	    } else {
		name = res.get().name;
	    }
	    return name;
	}
    }
    
}
