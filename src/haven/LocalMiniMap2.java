package haven;

import static haven.MCache.*;
import static haven.Radar.*;

public class LocalMiniMap2 extends LocalMiniMap {
    private String biome;
    private Tex biometex;
    
    public LocalMiniMap2(Coord sz, MapView mv) {
	super(sz, mv);
    }
    
    @Override
    public void draw(GOut g) {
	super.draw(g);
	if(CFG.MMAP_SHOW_BIOMES.get()) {
	    if(biometex != null) {g.image(biometex, Coord.z);}
	}
    }
    
    public void drawicons(GOut g) {
	Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
	Radar.draw(g, this::p2c, pl != null ? pl.rc : null);
    }
    
    public Gob findicongob(Coord c) {
	synchronized (markers) {
	    for (int i = markers.size() - 1; i >= 0; i--) {
		try {
		    Radar.Marker icon = markers.get(i);
		    Coord gc = p2c(icon.gob.rc);
		    Tex tex = icon.tex();
		    if(tex != null) {
			Coord sz = tex.sz();
			if(c.isect(gc.sub(sz.div(2)), sz))
			    return icon.gob;
		    }
		} catch (Loading ignored) {}
	    }
	}
	return (null);
    }
    
    @Override
    public Object tooltip(Coord c, Widget prev) {
	Gob gob = findicongob(c);
	if(gob != null) {
	    Radar.Marker icon = gob.getattr(Radar.Marker.class);
	    if(icon != null) {
		return icon.tooltip(false);
	    }
	}
	return super.tooltip(c, prev);
    }
    
    public void tick(double dt) {
	super.tick(dt);
	Coord mc = rootxlate(ui.mc);
	if(mc.isect(Coord.z, sz)) {
	    setBiome(c2p(mc).div(tilesz));
	} else {
	    setBiome(cc);
	}
    }
    
    private void setBiome(Coord c) {
	try {
	    if(c.div(cmaps).manhattan2(cc.div(cmaps)) > 1) {return;}
	    int t = mv.ui.sess.glob.map.gettile(c);
	    Resource r = ui.sess.glob.map.tilesetr(t);
	    String newbiome;
	    if(r != null) {
		newbiome = (r.name);
	    } else {
		newbiome = "Void";
	    }
	    if(!newbiome.equals(biome)) {
		biome = newbiome;
		biometex = Text.renderstroked(prettybiome(biome)).tex();
	    }
	} catch (Loading ignored) {}
    }
    
    private static String prettybiome(String biome) {
	int k = biome.lastIndexOf("/");
	biome = biome.substring(k + 1);
	biome = biome.substring(0, 1).toUpperCase() + biome.substring(1);
	return biome;
    }
}
