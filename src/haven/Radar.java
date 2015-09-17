package haven;

import java.util.LinkedList;
import java.util.List;

public class Radar {
    public static final List<Marker> markers =  new LinkedList<>();
    private static boolean dirty = false;

    public static void add(Gob gob, Indir<Resource> res) {
	if(gob.getattr(Marker.class) == null) {
	    Marker marker = new Marker(gob, res);
	    gob.setattr(marker);
	    synchronized (markers) {
		markers.add(marker);
		dirty = true;
	    }
	}
    }

    private static Tex tex(String resname) {
	if(resname == null){return null;}
	try {
	    if(resname.equals("gfx/terobjs/items/arrow")) {
		System.out.println(resname);
		try {
		    return loadres("gfx/invobjs/arrow-bone").layer(Resource.imgc).tex();
		}catch(Loading ignored){}
		//return Resource.loadtex("gfx/invobjs/arrow-bone");
	    }
	}catch(Loading ignored){}
	return null;
    }

    public static void tick() {
	synchronized (markers){
	    if(dirty){
		//markers.sort();
	    }
	}
    }

    public static void remove(Gob gob) {
	if(gob != null) {
	    synchronized (markers) {
		markers.remove(gob.getattr(Marker.class));
		gob.delattr(Marker.class);
	    }
	}
    }

    private static Resource loadres(String name) {
	return Resource.remote().load(name).get();
    }

    public static class Marker extends GAttrib {
	private final Indir<Resource> res;
	private Tex tex;

	public Marker(Gob gob, Indir<Resource> res) {
	    super(gob);
	    this.res = res;
	}

	public Tex tex() {
	    GobIcon gi = gob.getattr(GobIcon.class);
	    if(gi != null) {
		return gi.tex();
	    }
	    if(tex == null) {
		tex = Radar.tex(resname());
	    }
	    return tex;
	}

	private String resname() {
	    String name = null;
	    if(res instanceof Resource.Named) {
		name = ((Resource.Named) res).name;
	    } else {
		try {
		    name = res.get().name;
		} catch (Resource.Loading ignored) {
		}
	    }
	    return name;
	}
    }

}
