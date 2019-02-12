package haven;

public class DraggableWidget extends Widget {
    
    private final String name;
    private UI.Grab dm;
    private Coord doff;
    private Window.WndCfg cfg;
    private boolean draggable = true;
    
    public DraggableWidget(String name) {
	this.name = name;
    }
    
    public void draggable(boolean draggable) {
	this.draggable = draggable;
	if(!draggable) {stop_dragging();}
    }
    
    private void stop_dragging() {
	if(dm != null) {
	    dm.remove();
	    dm = null;
	    updateCfg();
	}
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	if(super.mousedown(c, button)) {
	    parent.setfocus(this);
	    return true;
	}
	if(c.isect(Coord.z, sz) && draggable) {
	    if(button == 1) {
		dm = ui.grabmouse(this);
		doff = c;
	    }
	    parent.setfocus(this);
	    return true;
	}
	return false;
    }
    
    @Override
    public boolean mouseup(Coord c, int button) {
	if(dm != null) {
	    stop_dragging();
	} else {
	    super.mouseup(c, button);
	}
	return (true);
    }
    
    @Override
    public void mousemove(Coord c) {
	if(dm != null) {
	    this.c = this.c.add(c.add(doff.inv()));
	} else {
	    super.mousemove(c);
	}
    }
    
    protected void added() {
	initCfg();
    }
    
    protected void initCfg() {
	cfg = Window.WndCfg.get(name);
	if(cfg != null) {
	    c = cfg.c;
	} else {
	    updateCfg();
	}
    }
    
    protected void updateCfg() {
	setCfg();
	storeCfg();
    }
    
    protected void setCfg() {
	if(cfg == null) {
	    cfg = new Window.WndCfg();
	}
	cfg.c = c;
    }
    
    protected void storeCfg() {
	Window.WndCfg.set(name, cfg);
    }
}
