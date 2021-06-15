package me.ender.minimap;

import haven.*;

import java.awt.*;
import java.util.Objects;

public class PMarker extends Marker {
    public static final Resource.Image flagbg, flagfg;
    public static final Coord flagcc;
    
    static {
	Resource flag = Resource.local().loadwait("gfx/hud/mmap/flag");
	flagbg = flag.layer(Resource.imgc, 1);
	flagfg = flag.layer(Resource.imgc, 0);
	flagcc = UI.scale(flag.layer(Resource.negc).cc);
    }
    
    public Color color;
    
    public PMarker(long seg, Coord tc, String nm, Color color) {
	super(seg, tc, nm);
	this.color = color;
    }
    
    @Override
    public boolean equals(Object o) {
	if(this == o) return true;
	if(o == null || getClass() != o.getClass()) return false;
	if(!super.equals(o)) return false;
	PMarker pMarker = (PMarker) o;
	return color.equals(pMarker.color);
    }
    
    @Override
    public void draw(final GOut g, final Coord c, final Text tip, final float scale, final MapFile file) {
	final Coord ul = c.sub(flagcc);
	g.chcolor(color);
	g.image(flagfg, ul);
	g.chcolor();
	g.image(flagbg, ul);
	if(CFG.MMAP_SHOW_MARKER_NAMES.get()) {
	    final Coord tipc = new Coord(ul.x + flagbg.img.getWidth() / 2 - tip.sz().x / 2,
		ul.y - tip.sz().y);
	    g.image(tip.tex(), tipc);
	}
    }
    
    @Override
    public Area area() {
	return Area.sized(flagcc.inv(), UI.scale(flagbg.sz));
    }
    
    @Override
    public int hashCode() {
	return Objects.hash(super.hashCode(), color);
    }
}
