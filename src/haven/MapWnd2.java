package haven;

public class MapWnd2 extends MapWnd {
    private boolean moved = false;
    private MapFileWidget.Location clickloc = null;
    
    public MapWnd2(MapFile file, MapView mv, Coord sz, String title) {
	super(file, mv, sz, title);
	
	addtwdg(add(new IButton("gfx/hud/mmap/grid", "", "-d", "-h") {
	    {tooltip = Text.render("toggle list");}
	    
	    public void click() {
		container.show(!container.visible);
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
	viewf.hide();
	
    }
    
    @Override
    public void resize(Coord sz) {
	asz = sz;
	super.resize(sz);
	view.resize(sz);
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	moved = false;
	return super.mousedown(c, button);
    }
    
    @Override
    protected void clickloc(MapFileWidget.Location loc) {
	clickloc = loc;
    }
    
    @Override
    public void mousemove(Coord c) {
	super.mousemove(c);
	moved = true;
    }
    
    @Override
    public boolean mouseup(Coord c, int button) {
	if(!domark && !moved) {
	    //todo: map clicked, find where to go or gob to interact with
	    try {
		MapFileWidget.Location curloc = view.curloc;
		if(curloc != null && clickloc !=null) {
		    Coord tc = c.sub(view.sz.div(2)).add(curloc.tc);
		    Coord pos = view.mappos(clickloc, player);
		    if(pos != null) {
			mv.wdgmsg("click", rootpos().add(c), pos, button, ui.modflags());
		    }
		}
	    } catch (Exception e) {e.printStackTrace();}
	}
	clickloc = null;
	return super.mouseup(c, button);
    }
}
