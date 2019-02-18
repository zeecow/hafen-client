package haven;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class GobDamageInfo extends GobInfo {
    private static Map<Long, Integer> gobDamage = new LinkedHashMap<Long, Integer>() {
	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
	    return size() > 50;
	}
    };
    
    private int damage = 0;
    
    public GobDamageInfo(Gob owner) {
	super(owner);
	up = 12;
	center = new Pair<>(0.5, 1.0);
	if(gobDamage.containsKey(gob.id)) {
	    damage = gobDamage.get(gob.id);
	}
    }
    
    @Override
    public boolean setup(RenderList d) {
	return super.setup(d);
    }
    
    @Override
    public Object staticp() {
	return null;
    }
    
    @Override
    protected Tex render() {
	if(damage > 0) {
	    return Text.std.renderstroked(String.format("%d", damage), Color.RED, Color.BLACK).tex();
	}
	return null;
    }
    
    public void update(int c, int v) {
	//System.out.println(String.format("Number %d, c: %d", v, c));
	//64527 - SHP
	//61455 - HHP
	//35071 - Initiative
	if(c == 61455) {//health
	    damage += v;
	    gobDamage.put(gob.id, damage);
	    if(tex != null) {
		tex.dispose();
	    }
	    tex = render();
	}
    }
    
    public static boolean has(Gob gob) {
	return gobDamage.containsKey(gob.id);
    }
    
    private static void clearDamage(Gob gob, long id) {
	if(gob != null) {
	    gob.clearDmg();
	}
	gobDamage.remove(id);
    }
    
    public static void clearPlayerDamage(GameUI gui) {
	clearDamage(gui.ui.sess.glob.oc.getgob(gui.plid), gui.plid);
    }
    
    public static void clearAllDamage(GameUI gui) {
	for (long id : gobDamage.keySet()) {
	    clearDamage(gui.ui.sess.glob.oc.getgob(id), id);
	}
    }
}
