package haven;


import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static haven.MCache.*;

public class MapWnd2 extends MapWnd {
    private boolean switching = true;
    private final Map<Long, GobMarker> markers = new HashMap<>();
    
    public MapWnd2(MapFile file, MapView mv, Coord sz, String title) {
	super(file, mv, sz, title);
    }
    
    @Override
    protected String cfgName(String cap) {
	return super.cfgName(cap) + (compact() ? ".compact" : "");
    }
    
    private boolean compact() {
	return tool != null && !tool.visible;
    }
    
    @Override
    public void compact(boolean a) {
	switching = true;
	storeCfg();
	super.compact(a);
	String name = cfgName(caption());
	cfg = WidgetCfg.get(name);
	initCfg();
	switching = false;
    }
    
    @Override
    public void resize(Coord sz) {
	super.resize(sz);
	cfg.sz = asz;
	if(!switching) {storeCfg();}
    }
    
    @Override
    protected void initCfg() {
	if(cfg != null && cfg.sz != null) {
	    asz = cfg.sz;
	    super.resize(asz);
	}
	super.initCfg();
    }
    
    @Override
    protected void setCfg() {
	super.setCfg();
	cfg.sz = asz;
    }
    
    public void addMarker(Gob gob) {
	addMarker(gob.rc.floor(tilesz), gob.tooltip());
    }
    
    public void addMarker(Coord at) {
	addMarker(at, "New marker");
    }
    
    public void addMarker(Coord at, String name) {
	at = at.add(view.sessloc.tc);
	MapFile.Marker nm = new MapFile.PMarker(view.sessloc.seg.id, at, name, BuddyWnd.gc[new Random().nextInt(BuddyWnd.gc.length)]);
	file.add(nm);
	focus(nm);
	if(ui.modctrl) {
	    ui.gui.track(nm);
	}
	domark = false;
    }
    
    public void removeMarker(MapFile.Marker marker) {
	if(tool.list.sel == marker) {
	    if(mremove != null) {
		mremove.click();
	    } else {
		view.file.remove(marker);
		ui.gui.untrack(marker);
	    }
	}
    }
    
    public void updateGobMarkers() {
	Map<Long, GobMarker> markers;
	synchronized (this.markers) {
	    markers = new HashMap<>(this.markers);
	}
	markers.values().forEach(GobMarker::update);
    }
    
    public void track(Gob gob) {
	GobMarker marker;
	synchronized (this.markers) {
	    if(markers.containsKey(gob.id)) {
		marker = markers.get(gob.id);
	    } else {
		marker = new GobMarker(gob);
		markers.put(gob.id, marker);
	    }
	}
	ui.gui.track(marker);
	domark = false;
    }
    
    public void untrack(long gobid) {
	synchronized (markers) {
	    markers.remove(gobid);
	}
    }
    
    public class GobMarker extends MapFile.Marker {
	public final long gobid;
	public final Indir<Resource> res;
	private Coord2d rc = null;
	public final Color col;
	
	public GobMarker(Gob gob) {
	    super(0, gob.rc.floor(tilesz), gob.tooltip());
	    this.gobid = gob.id;
	    GobIcon icon = gob.getattr(GobIcon.class);
	    res = (icon == null) ? null : icon.res;
	    col = color(gob);
	}
	
	private Color color(Gob gob) {
	    if(gob.anyOf(GobTag.FOE, GobTag.AGGRESSIVE)) {
		return new Color(220, 100, 100);
	    } else if(gob.is(GobTag.FRIEND)) {
		return new Color(100, 220, 100);
	    } else if(gob.is(GobTag.ANIMAL)) {
		return new Color(100, 200, 220);
	    }
	    return Color.LIGHT_GRAY;
	}
	
	private void update() {
	    Gob gob = ui.sess.glob.oc.getgob(gobid);
	    if(gob != null) {
		seg = view.sessloc.seg.id;
		rc = gob.rc;
		try {
		    tc = rc.floor(tilesz).add(view.sessloc.tc);
		} catch (Exception ignore) {}
	    }
	}
	
	public Coord2d rc() {
	    return rc;
	}
    
	public boolean hide() {
	    Gob gob = ui.sess.glob.oc.getgob(gobid);
	    Gob player = ui.gui.map.player();
	    if(gob != null && player != null) {
		if(gob.drivenByPlayer) {
		    return true;
		}
		if(gob.moving instanceof Following) {
		    return ((Following) gob.moving).tgt == gob.glob.sess.ui.gui.map.plgob;
		} else if(gob.moving instanceof Homing) {
		    Homing homing = (Homing) gob.moving;
		    return homing.dist < 30 && homing.tgt == gob.glob.sess.ui.gui.map.plgob;
		}
		return gob.is(GobTag.VEHICLE) && gob.rc.dist(player.rc) < 25;
	    }
	    return false;
	}
    }
}
