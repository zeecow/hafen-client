package haven;

import java.awt.*;

public class GobHighlight extends GAttrib {
    private static final Color COLOR = new Color(64, 255, 64, 255);
    private static final long cycle = 1200;
    private static final long duration = 7500;
    private long start = System.currentTimeMillis();
    
    public GobHighlight(Gob g) {
	super(g);
    }
    
    @Override
    public Object staticp() {
	return null;
    }
    
    public GLState getfx() {
	long active = System.currentTimeMillis() - start;
	if(active > duration) {
	    gob.delattr(GobHighlight.class);
	    return Material.nullstate;
	} else {
	    float k = (float) Math.abs(Math.sin(Math.PI * active / cycle));
	    Material.Colors colors = new Material.Colors(COLOR);
	    colors.spc = colors.emi = Utils.c2fa(Utils.blendcol(Color.DARK_GRAY, COLOR, k));
	    return colors;
	}
    }
}