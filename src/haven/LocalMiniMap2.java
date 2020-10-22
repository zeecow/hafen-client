package haven;

import java.util.List;

import static haven.MCache.*;

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
    
    @Override
    public Object tooltip(Coord c, Widget prev) {
	Gob gob = findicongob(c);
	if(gob != null) {
	    GobIcon icon = gob.getattr(GobIcon.class);
	    if(icon != null) {
		return icon.tooltip();
	    }
	}
	return super.tooltip(c, prev);
    }
    
    public void tick(double dt) {
	super.tick(dt);
	Coord mc = rootxlate(ui.mc);
	if(mc.isect(Coord.z, sz)) {
	    setBiome(c2p(mc).div(tilesz).floor());
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
