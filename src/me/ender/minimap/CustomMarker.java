package me.ender.minimap;

import haven.*;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

// Simple custom icons that are a combo of PMarker (color) and SMarker (Custom res)
public class CustomMarker extends Marker {
    private static final Map<Indir<Resource>, Image> cache = new WeakHashMap<>();
    
    public Color color;
    public final Resource.Spec res;
    
    public CustomMarker(final long seq, final Coord tc, final String nm,
			final Color color, final Resource.Spec res) {
	super(seq, tc, nm);
	this.color = color;
	this.res = res;
    }
    
    public char identifier() {
	return 'r';
    }
    
    public int version() {
	return 1;
    }
    
    @Override
    public boolean equals(Object o) {
	if(this == o) return true;
	if(o == null || getClass() != o.getClass()) return false;
	if(!super.equals(o)) return false;
	CustomMarker that = (CustomMarker) o;
	return color.equals(that.color) && res.equals(that.res);
    }
    
    @Override
    public void draw(GOut g, Coord c, Text tip, final float scale, final MapFile file) {
	final Image img = image();
	if(img != null) {
	    g.chcolor(color);
	    final Coord ul = c.sub(img.cc);
	    g.image(img.tex, ul);
	    if(CFG.MMAP_SHOW_MARKER_NAMES.get()) {
		g.aimage(tip.tex(), c.addy(UI.scale(5)), 0.5, 0);
	    }
	    g.chcolor();
	}
    }
    
    @Override
    public Area area() {
	final Image img = image();
	if(img == null) {return null;}
	Coord sz = img.tex.sz();
	return Area.sized(sz.div(2).inv(), sz);
    }
    
    public Image image() {
	Image image = cache.get(this.res);
	if(image == null) {
	    try {
		final Resource res = Resource.loadsaved(Resource.remote(), this.res);
		final Resource.Image img = res.layer(Resource.imgc);
		image = new Image(img);
		cache.put(this.res, image);
	    } catch (Loading ignored) {}
	}
	return image;
    }
    
    @Override
    public int hashCode() {
	return Objects.hash(super.hashCode(), color, res);
    }
    
    private static class Image {
	final Tex tex;
	final Coord cc;
	
	public Image(Resource.Image img) {
	    this.tex = img.tex();
	    this.cc = tex.sz().div(2);
	}
    }
}
