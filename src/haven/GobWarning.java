package haven;

import haven.render.RenderTree;

import java.util.HashMap;
import java.util.Map;

import static haven.GobWarning.WarnMethod.*;
import static haven.GobWarning.WarnTarget.*;

public class GobWarning extends GAttrib implements RenderTree.Node {
    private final ColoredRadius radius;
    private final WarnTarget tgt;
    
    public GobWarning(Gob gob) {
	super(gob);
	String what = "Danger";
	if(gob.is(GobTag.FOE)) {
	    tgt = player;
	    what = "Player";
	} else if(gob.is(GobTag.AGGRESSIVE)) {
	    tgt = animal;
	    what = "Dangerous animal";
	} else {
	    tgt = null;
	}
	if(WarnCFG.get(tgt, message)) {
	    gob.glob.sess.ui.message(String.format("%s spotted!", what), GameUI.MsgType.ERROR);
	}
	radius = new ColoredRadius(gob, 50);
    }
    
    @Override
    public void added(RenderTree.Slot slot) {
	super.added(slot);
	if(WarnCFG.get(tgt, highlight)) {slot.add(radius);}
    }
    
    public enum WarnTarget {
	player, animal
    }
    
    public enum WarnMethod {
	highlight, message
    }
    
    private static class WarnCFG {
	
	static boolean get(WarnTarget target, WarnMethod method) {
	    if(target != null) {
		Map<String, Boolean> cfg = CFG.WARN_CONFIG.get().getOrDefault(target.name(), new HashMap<>());
		return cfg.getOrDefault(method.name(), false);
	    }
	    return false;
	}
	
	static void set(WarnTarget target, WarnMethod method, boolean value) {
	    Map<String, Map<String, Boolean>> cfg = CFG.WARN_CONFIG.get();
	    Map<String, Boolean> tcfg = cfg.getOrDefault(target.name(), new HashMap<>());
	    tcfg.put(method.name(), value);
	    cfg.put(target.name(), tcfg);
	    CFG.WARN_CONFIG.set(cfg);
	    
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
	    
	    //TODO: Make this pretty
	    CheckBox box = add(new CheckBox("Highlight players", false), 0, y);
	    box.a = WarnCFG.get(player, highlight);
	    box.changed(val -> WarnCFG.set(player, highlight, val));
	    y += 25;
	    
	    box = add(new CheckBox("Warn about players", false), 0, y);
	    box.a = WarnCFG.get(player, message);
	    box.changed(val -> WarnCFG.set(player, message, val));
	    y += 35;
	    
	    box = add(new CheckBox("Highlight animals", false), 0, y);
	    box.a = WarnCFG.get(WarnTarget.animal, highlight);
	    box.changed(val -> WarnCFG.set(animal, highlight, val));
	    y += 25;
	    
	    box = add(new CheckBox("Warn about animals", false), 0, y);
	    box.a = WarnCFG.get(WarnTarget.animal, message);
	    box.changed(val -> WarnCFG.set(animal, message, val));
	    
	    pack();
	    if(asz.x < 200) {
		resize(new Coord(200, asz.y));
	    }
	}
    }
}
