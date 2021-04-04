package haven;

import haven.render.RenderTree;

public class GobWarning extends GAttrib implements RenderTree.Node {
    public static final WarnCFG DEFAULT = new WarnCFG(
	new WarnVO(false, true),
	new WarnVO(true, false)
    );
    
    private final ColoredRadius radius;
    private final WarnVO cfg;
    
    public GobWarning(Gob gob) {
	super(gob);
	String what = "Danger";
	if(gob.is(GobTag.FOE)) {
	    cfg = CFG.WARN_CONFIG.get().player;
	    what = "Player";
	} else if(gob.is(GobTag.AGGRESSIVE)) {
	    cfg = CFG.WARN_CONFIG.get().animal;
	    what = "Dangerous animal";
	} else {
	    cfg = new WarnVO();
	}
	if(cfg.message) {
	    gob.glob.sess.ui.message(String.format("%s spotted!", what), GameUI.MsgType.ERROR);
	}
	radius = new ColoredRadius(gob, 50);
    }
    
    @Override
    public void added(RenderTree.Slot slot) {
	super.added(slot);
	if(cfg.highlight) {slot.add(radius);}
    }
    
    public static class WarnVO {
	public boolean highlight;
	public boolean message;
	
	public WarnVO() {
	    this(false, false);
	}
	
	public WarnVO(boolean h, boolean m) {
	    highlight = h;
	    message = m;
	}
    }
    
    public static class WarnCFG {
	public WarnVO player;
	public WarnVO animal;
	
	public WarnCFG(WarnVO p, WarnVO a) {
	    player = p;
	    animal = a;
	}
    }
    
    public static class WarnCFGWnd extends Window {
	private static Window instance;
	
	public static void toggle(Widget parent) {
	    if(instance == null) {
		instance = parent.add(new WarnCFGWnd());
	    } else {
		doClose();
	    }
	}
	
	private static void doClose() {
	    if(instance != null) {
		instance.reqdestroy();
		instance = null;
	    }
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    instance = null;
	}
	
	public WarnCFGWnd() {
	    super(Coord.z, "Warn settings");
	    justclose = true;
	    int y = 0;
	    WarnCFG cfg = CFG.WARN_CONFIG.get();
	    
	    //TODO: Make this pretty
	    CheckBox box = add(new CheckBox("Highlight players", false), 0, y);
	    box.a = cfg.player.highlight;
	    box.changed(val -> {
		cfg.player.highlight = val;
		CFG.WARN_CONFIG.set(cfg);
	    });
	    y += 25;
	    
	    box = add(new CheckBox("Warn about players", false), 0, y);
	    box.a = cfg.player.message;
	    box.changed(val -> {
		cfg.player.message = val;
		CFG.WARN_CONFIG.set(cfg);
	    });
	    y += 35;
	    
	    box = add(new CheckBox("Highlight animals", false), 0, y);
	    box.a = cfg.animal.highlight;
	    box.changed(val -> {
		cfg.animal.highlight = val;
		CFG.WARN_CONFIG.set(cfg);
	    });
	    y += 25;
	    
	    box = add(new CheckBox("Warn about animals", false), 0, y);
	    box.a = cfg.animal.message;
	    box.changed(val -> {
		cfg.animal.message = val;
		CFG.WARN_CONFIG.set(cfg);
	    });
	    
	    pack();
	    if(asz.x < 200) {
		resize(new Coord(200, asz.y));
	    }
	}
    }
}
