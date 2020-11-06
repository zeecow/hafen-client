package haven;

import java.util.List;

public class MapWnd2 extends MapWnd {
    
    public MapWnd2(MapFile file, MapView mv, Coord sz, String title) {
	super(file, mv, sz, title);
	
	addtwdg(add(new IButton("gfx/hud/mmap/list", "", "-d", "-h") {
	    {tooltip = Text.render("toggle list");}
	    
	    public void click() {
//		container.show(!container.visible);
//		CFG.MMAP_LIST.set(container.visible);
	    }
	}));
	
	addtwdg(add(new IButton("gfx/hud/mmap/view", "", "-d", "-h") {
	    {tooltip = Text.render("Display view distance");}
	    
	    public void click() {
		CFG.MMAP_VIEW.set(!CFG.MMAP_VIEW.get());
	    }
	}));
	
	addtwdg(add(new IButton("gfx/hud/mmap/grid", "", "-d", "-h") {
	    {tooltip = Text.render("Display grid");}
	    
	    public void click() {
		CFG.MMAP_GRID.set(!CFG.MMAP_GRID.get());
	    }
	}));
	
	view.unlink();
	add(view);
	view.lower();
//	viewf.hide();
//	container.show(CFG.MMAP_LIST.get());
    }
    
    @Override
    public void resize(Coord sz) {
	asz = sz;
	super.resize(sz);
	view.resize(sz);
    }
    
    @Override
    protected void initCfg() {
	if(cfg != null && cfg.sz != null){
	    asz = cfg.sz;
	    resize(asz);
	}
	super.initCfg();
    }
    
    @Override
    protected void setCfg() {
	super.setCfg();
	cfg.sz = asz;
    }
    
}
