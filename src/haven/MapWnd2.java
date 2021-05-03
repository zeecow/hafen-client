package haven;


import java.util.Random;

import static haven.MCache.*;

public class MapWnd2 extends MapWnd {
    private boolean switching = true;
    
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
	String name = cfgName(caption());
	storeCfg();
	super.compact(a);
	name = cfgName(caption());
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
    
}
